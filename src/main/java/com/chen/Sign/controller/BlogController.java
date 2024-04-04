package com.chen.Sign.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.Sign.common.GuiguException;
import com.chen.Sign.common.Result;
import com.chen.Sign.constants.MqConstants;
import com.chen.Sign.mapper.*;
import com.chen.Sign.utils.SimilarityRatioUtils;
import com.chen.Sign.pojo.*;
import com.chen.Sign.service.impl.BlogServiceImpl;
import com.chen.Sign.service.impl.UserServiceImpl;
import com.chen.Sign.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

// 博客主模块
@RestController
@Slf4j
@SuppressWarnings({"all"})
@RequestMapping("/blog")
@CrossOrigin
@Component
public class BlogController {

    @Value("${lab.path}")
    private String basePath;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    BlogServiceImpl blogService;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    BlogMapper blogMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    private BlogsumdataMapper blogsumdataMapper;

    @Autowired
    private User_likeMapper user_likeMapper;

    @Autowired
    private CollectMapper collectMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private BlogimageMapper blogimageMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 根据文章标题返回推荐的文章，Levenshtein Distance算法
    public Object recommend(String title, int length) {
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Blog::getHot);
        List<Blog> blogs = blogMapper.selectList(queryWrapper);
        String data[][] = new String[blogs.size()][3];
        for (int i = 0; i < blogs.size(); i++) {
            Blog blog = blogs.get(i);
            String title1 = blog.getTitle();
            String id = String.valueOf(blog.getId());
            float similarityRatio = SimilarityRatioUtils.getSimilarityRatio(title, title1);
            data[i][0] = title1;
            data[i][1] = String.valueOf(similarityRatio);
            data[i][2] = id;
        }
        //二维数组冒泡排序
        for (int i = 0; i < data.length - 1; i++) {
            for (int j = 0; j < data.length - 1 - i; j++) {
                if (Float.parseFloat(data[j][1]) < Float.parseFloat(data[j + 1][1])) {
                    String[][] temp = new String[1][2];
                    temp[0] = data[j];
                    data[j] = data[j + 1];
                    data[j + 1] = temp[0];
                }
            }
        }
        if (length > blogs.size()) {
            String result[][] = new String[blogs.size()][2];
            for (int i = 0; i < blogs.size(); i++) {
                result[i][0] = data[i][0];
                result[i][1] = data[i][2];
            }
            return result;
        }
        String result[][] = new String[length][2];
        for (int i = 0; i < length; i++) {
            result[i][0] = data[i][0];
            result[i][1] = data[i][2];
        }
        return result;
    }

    // 算法文章的热度，hacker news的排名算法,每天晚上0点触发
    //@Scheduled(cron = "0 0/5 * * * ?")
    @Scheduled(cron = "0 0 0 * * *")
    public void hot() {
        System.out.println("每五分钟执行一次开始");
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String time1 = dateFormat.format(calendar.getTime());
            Date time = dateFormat.parse(time1);
            List<Blog> blogs = blogMapper.selectList(null);
            for (Blog blog : blogs) {
                Date create_time = (Date) dateFormat.parse(blog.getCreate_time());
                Integer views = blog.getViews();
                int differHour = getDifferHour(create_time, time);
                System.out.println(differHour);
                Double counthot = counthot(views, differHour);
                blog.setHot(counthot);
                blogMapper.updateById(blog);
                System.out.println("每五分钟执行一次结束");
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    // 计算热度
    public Double counthot(Integer view, Integer time) {
        System.out.println("执行结果:");
        System.out.println("view:" + view + "time" + time);
        Double hot = (view - 1) / Math.pow(time + 2, 2);
        System.out.println(hot);
        return hot;
    }

    // 计算时间
    // 24 * (differ / dayM) 这里拿到被舍弃的整数，整数是几，就代表相隔几天，一天24小时，那就整数乘以24即可。
    private int getDifferHour(Date startDate, Date endDate) {
        System.out.println(startDate);
        System.out.println(endDate);
        long dayM = 1000 * 24 * 60 * 60;
        long hourM = 1000 * 60 * 60;
        long differ = endDate.getTime() - startDate.getTime();
        long hour = differ % dayM / hourM + 24 * (differ / dayM);
        return Integer.parseInt(String.valueOf(hour));
    }

    // 添加博客--√
    @PostMapping("/add")
    public Result addBlog(HttpServletRequest request) throws ParseException {
        Result result = new Result();
        // 获取token中的信息
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        User user = userService.selectallinformation(username);
        // 读取前端传回的博客数据
        String nickname = user.getNickname();
        String profile_photo = user.getProfile_photo();
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String tag = request.getParameter("tag");
        String filename = request.getParameter("filename");
        String link = request.getParameter("link");
        if (title == null || content == null || tag == null) {
            throw new GuiguException(0, "有参数未填写", "");
        }
        // 添加博客使用标签次数start
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        LambdaUpdateWrapper<Tag> objectLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        String[] split = tag.split(",");
        for (String s : split) {
            queryWrapper.eq(Tag::getTagname, s);
            Tag tag1 = tagMapper.selectOne(queryWrapper);
            Integer sum = tag1.getSum();
            tag1.setSum(++sum);
            tagMapper.updateById(tag1);
        }
        // 添加博客使用标签次数end
        Integer views = 0;
        // 获取时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(System.currentTimeMillis());
        // 创建实体类
        Blog blog = new Blog();
        blog.setUsername(username);
        blog.setNickname(nickname);
        blog.setProfile_photo(profile_photo);
        blog.setTitle(title);
        blog.setContent(content);
        blog.setTag(tag);
        blog.setFilename(filename);
        blog.setLink(link);
        blog.setCreate_time(date);
        blog.setUpdate_time(date);
        int insert = blogMapper.insert(blog);
        Map<String, Object> map = new HashMap<>();
        if (insert > 0) {
            map.put("nowblogdata", blog);
            // 发送消息到消息队列
            rabbitTemplate.convertAndSend(MqConstants.SIGN_EXCHANGE,MqConstants.SIGN_INSERT_KEY,blog.getId());
            return result.insertBlogok(map);
        } else {
            return result.insertBlogno(map);
        }
    }

    /**
     * 添加博客图片
     *
     * @param id
     * @return
     */
    @PostMapping("/addimage")
    public Result addimage(@RequestPart("file") MultipartFile file, HttpServletRequest request) throws IOException {
        long starttime = System.currentTimeMillis();
        log.info(file.toString());

        // 原始文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String filename = UUID.randomUUID().toString() + suffix;

        // 创建一个目录对象，判断是否存在，不存在则创建
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            file.transferTo(new File(basePath + filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String address = "/images/" + filename;
        long endtime = System.currentTimeMillis();
        log.info("上传文件花费的时间为：" + (endtime - starttime) + "毫秒");
        return new Result(1, "文件上传成功", address);
    }

    // 查看博客--√
    @GetMapping({"/see/{id}"})
    public Result getById(@PathVariable("id") Integer id, HttpServletRequest request) {
        String gttustate = "0";
        String collectstate = "0";
        // 获取token中的信息
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String uid = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        LambdaQueryWrapper<Blog> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Blog::getId, id);
        Blog blog2 = blogMapper.selectOne(queryWrapper1);
        if (blog2 != null) {
            // 获取博客主要内容
            Blog blog = blogMapper.selectById(id);
            String title = blog.getTitle();
            // 获取博客标签
            // 获取访问量
            Integer views = blog.getViews();
            // 访问量
            ++views;
            // 修改数据库中的访问量数据
            blogService.updateviews(views, id);
            Blog blog1 = blogService.getById(id);
            Object recommend = recommend(title, 5);
            // 查找该用户是否点赞
            User_like user_like = user_likeMapper.selectgttustate(Integer.valueOf(uid), id);
            if (user_like != null) {
                gttustate = "1";
            }
            // 查找该用户是否收藏
            Collect collect = collectMapper.selectcollectstate(username, id);
            if (collect != null) {
                collectstate = "1";
            }
            HashMap<String, Object> map = new HashMap<>();
            map.put("recommend", recommend);
            map.put("blogdata", blog1);
            // 点赞的状态
            map.put("gttustate", gttustate);
            // 收藏的状态
            map.put("collectstate", collectstate);
            return new Result(1, "返回博客数据成功", map);
        }
        return new Result(1, "没有该博客", "");
    }


    // 修改博客--√
    @PutMapping("/modify/{id}")
    public Result modify(HttpServletRequest request, @PathVariable("id") Integer id) {
        Result result = new Result();
        Blog blog = blogService.getById(id);
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String filename = request.getParameter("filename");
        String tag = request.getParameter("tag");
        String link = request.getParameter("link");
        if (title == null || content == null || tag == null) {
            throw new GuiguException(0, "有必需参数未填写", "");
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(System.currentTimeMillis());
        blog.setTitle(title);
        blog.setContent(content);
        blog.setTag(tag);
        blog.setLink(link);
        blog.setUpdate_time(date);
        blogService.updateById(blog);
        rabbitTemplate.convertAndSend(MqConstants.SIGN_EXCHANGE,MqConstants.SIGN_INSERT_KEY,id);
        return result.modifyok("");
    }

    // 删除博客--√
    @DeleteMapping({"/delete/{id}"})
    public Result delete(HttpServletRequest request, @PathVariable("id") Integer id) {
        Result result = new Result();
        LambdaQueryWrapper<Blog> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Blog::getId, id);
        Blog blog = blogMapper.selectOne(queryWrapper1);
        if (blog != null) {
            // 删除该博客的静态资源
            String filename = blog.getFilename();
            if (filename != null && filename != "") {
                String[] split1 = filename.split(",");
                for (String s : split1) {
                    String[] split = s.split("/");
                    String filepath = "/usr/local/signt/images/" + s;
                    File file1 = new File(filepath);
                    if (file1.exists()) {
                        file1.delete();
                    }
                }
            }
            // 删除该博客
            blogMapper.deleteById(id);
            LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Collect::getBlog_id, id);
            List<Collect> collects = collectMapper.selectList(queryWrapper);
            List<Integer> idList = Arrays.asList();
            for (Collect collect : collects) {
                collectMapper.deleteById(collect.getId());
            }
            rabbitTemplate.convertAndSend(MqConstants.SIGN_EXCHANGE,MqConstants.SIGN_DELETE_KEY,id);
            return new Result(1, "删除博客成功", "");
        }
        return new Result(1, "没有该博客", "");
    }

    // 博客入口页--分页显示所有博客的标题和简介--pc端--
    @GetMapping("/getallblog")
    public Result getallblog(Integer page, Integer pageSize, HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        Page<Blog> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Blog::getHot);
        blogService.page(pageInfo, queryWrapper);
        return new Result(1, "查看数据成功", pageInfo);
    }

    // 博客入口页--懒加载显示所有博客的标题和简介--小程序端--√
    @GetMapping("/wxgetallblog")
    public Result wxgetallblog(HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(Blog::getId).orderByDesc(Blog::getHot);
        List<Blog> blogs = blogMapper.selectList(queryWrapper);
        return new Result(1, "博客入口页加载成功", blogs);
    }

    // 分页显示查看个人所有博客--pc端
    @GetMapping("/myallblog")
    public Result myallblog(Integer page, Integer pageSize, HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        Page<Blog> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blog::getUsername, username).orderByDesc(Blog::getHot);
        blogService.page(pageInfo, queryWrapper);
        return new Result(1, "查看数据成功", pageInfo);
    }

    // 懒加载显示查看个人所有博客--小程序端--√
    @GetMapping("/wxmyallblog")
    public Result wxmyallblog(HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        HashMap<String, Object> map = new HashMap<>();
        LambdaQueryWrapper<Blog> queryWrapper3 = new LambdaQueryWrapper<>();
        queryWrapper3.eq(Blog::getUsername, username);
        queryWrapper3.orderByDesc(Blog::getCreate_time);
        List<Blog> blogss = blogMapper.selectList(queryWrapper3);
        LambdaQueryWrapper<Blogsumdata> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(Blogsumdata::getUsername, username);
        Blogsumdata blogsumdata1 = blogsumdataMapper.selectOne(queryWrapper2);
        if (blogss.size() != 0) {
            // ----列表页上方信息
            // 总点赞数
            Integer sumgttu = 0;
            // 总访问量
            Integer sumviews = 0;
            LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
            LambdaQueryWrapper<Collect> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper.eq(Blog::getUsername, username);
            List<Blog> blogs = blogMapper.selectList(queryWrapper);
            for (Blog blog : blogs) {
                Integer gttu = blog.getGttu();
                Integer views = blog.getViews();
                sumgttu += gttu;
                sumviews += views;
            }
            queryWrapper1.eq(Collect::getUsername, username);
            List<Collect> collects = collectMapper.selectList(queryWrapper1);
            int size = collects.size();
            blogsumdata1.setSumgttu(sumgttu);
            blogsumdata1.setSumviews(sumviews);
            blogsumdata1.setSumcollect(size);
            int i = blogsumdataMapper.updateById(blogsumdata1);
            map.put("upperdata", blogsumdata1);
            map.put("blogdata", blogss);
            return new Result(1, "个人所有博客列表页加载成功", map);
        }
        map.put("upperdata", blogsumdata1);
        map.put("blogdata", blogss);
        return new Result(1, "没有博客数据", map);
    }

    // 搜索博客-pc端
    @PostMapping("/search")
    public Result search(HttpServletRequest request, Integer page, Integer pageSize) {
        String selectType = request.getParameter("selectType");
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String search = request.getParameter("search");
        insertSearchSort(id, search);
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Blog::getTitle, search);
        List<Blog> blogs = blogMapper.selectList(queryWrapper);
        return new Result(1, "搜索成功", blogs);
    }

    /**
     * 搜索博客-小程序端--√
     * 定义type 和 timeType 和 selectType
     * type 0:综合 1:文章
     * timeType 0:综合排序 1：最新排序 2：最热排序
     * selectType 0:时间不限 1：最新一天 2：最近一周 3：最近一月
     *
     * @param request
     * @return
     */
    @PostMapping("/wxsearch")
    public Result wxsearch(HttpServletRequest request) {
        String search = request.getParameter("search");
        Integer type = Integer.valueOf(request.getParameter("type"));
        String timeType = request.getParameter("timeType");
        String selectType = request.getParameter("selectType");
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        // 把用户ID当key，搜索内容当value 存入 Redis
        redisTemplate.opsForZSet().add(id, search, System.currentTimeMillis());
        // 调用下面的方法对存入Redis的数据进行处理
        insertSearchSort(id, search);
        List<Blog> blogs = new ArrayList<>();
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        if (timeType.equals("0") && selectType.equals("1")) {
            blogs = blogMapper.select01(search, type);
        } else if (timeType.equals("0") && selectType.equals("2")) {
            blogs = blogMapper.select02(search, type);
        } else if (timeType.equals("0") && selectType.equals("3")) {
            blogs = blogMapper.select03(search, type);
        } else if (timeType.equals("1") && selectType.equals("0")) {
            blogs = blogMapper.select10(search, type);
        } else if (timeType.equals("1") && selectType.equals("1")) {
            blogs = blogMapper.select11(search, type);
        } else if (timeType.equals("1") && selectType.equals("2")) {
            blogs = blogMapper.select12(search, type);
        } else if (timeType.equals("1") && selectType.equals("3")) {
            blogs = blogMapper.select13(search, type);
        } else if (timeType.equals("2") && selectType.equals("0")) {
            blogs = blogMapper.select20(search, type);
        } else if (timeType.equals("2") && selectType.equals("1")) {
            blogs = blogMapper.select21(search, type);
        } else if (timeType.equals("2") && selectType.equals("2")) {
            blogs = blogMapper.select22(search, type);
        } else if (timeType.equals("2") && selectType.equals("3")) {
            blogs = blogMapper.select23(search, type);
        } else {
            blogs = blogMapper.select00(search, type);
        }
        return new Result(1, "搜索成功", blogs);
    }

    /**
     * 查询搜索历史
     *
     * @return 列表查询历史记录，倒序
     */
    @GetMapping("/history")
    public Result selectSearchResultList(HttpServletRequest request) {
        List<String> searchList = new ArrayList<>();
        // 这里拿到用户的唯一ID作为KEY来给到Redis
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String key = tokenInfo.getClaim("id").asString();
        long start = 1;             // 指定开始区间值
        long size = 10;            // 指定长度区间值 （查询搜索历史记录最新的10条）
        Set<ZSetOperations.TypedTuple> scoreWithScores = redisTemplate.opsForZSet().reverseRangeWithScores(key, start - 1, size - 1);
        Iterator<ZSetOperations.TypedTuple> iterator = scoreWithScores.iterator();
        BigDecimal bigDecimal = null;
        while (iterator.hasNext()) {
            ZSetOperations.TypedTuple next = iterator.next();
            bigDecimal = BigDecimal.valueOf(next.getScore());
            if (next.getValue() != null) {
                searchList.add(next.getValue().toString());
            }
        }
        // 这里返回List给到前端
        return new Result(1, "返回搜索历史成功", searchList);
    }

    /**
     * 对传进来的搜索内容进行判断
     *
     * @param key
     * @param value
     */
    public void insertSearchSort(String key, String value) {
        //阈值-历史最多10个
        long top = 10;
        // 拿到存入Redis里数据的唯一分值
        Double score = redisTemplate.opsForZSet().score(key, value);
        //检索是否有旧记录  1.无则插入记录值  2.有则删除 再次插入
        if (null != score) {
            //删除旧的
            redisTemplate.opsForZSet().remove(key, value);
        }
        //加入新的记录，设置当前时间戳为分数score
        redisTemplate.opsForZSet().add(key, value, System.currentTimeMillis());
        //获取总记录数
        Long aLong = redisTemplate.opsForZSet().zCard(key);
        if (aLong > top) {
            //获取阈值200之后的记录  (0,1] 并移除
            redisTemplate.opsForZSet().removeRange(key, 0, aLong - top - 1);
        }
    }

    /**
     * 根据标签返回博客列表
     *
     * @param request
     * @return
     */
    @PostMapping("/tag")
    public Result tag(HttpServletRequest request) {
        String tag = request.getParameter("tag");
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Blog::getTag, tag).orderByDesc(Blog::getHot);
        List<Blog> blogs = blogMapper.selectList(queryWrapper);
        return new Result(1, "返回数据成功", blogs);
    }
}
