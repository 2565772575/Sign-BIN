package com.chen.Sign.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.Sign.common.Result;
import com.chen.Sign.pojo.*;
import com.chen.Sign.mapper.BoilMapper;
import com.chen.Sign.mapper.Boil_tagMapper;
import com.chen.Sign.mapper.BoilcollectMapper;
import com.chen.Sign.mapper.BoilsumdataMapper;
import com.chen.Sign.service.impl.BoilServiceImpl;
import com.chen.Sign.service.impl.UserServiceImpl;
import com.chen.Sign.utils.JWTUtils;
import com.chen.Sign.utils.SimilarityRatioUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 陈帅彬
 * @date 2023/4/29 11:40
 */
@RestController
@Slf4j
@SuppressWarnings({"all"})
@RequestMapping("/boil")
@CrossOrigin
@Component
public class BoilController {

    @Value("${lab.path}")
    private String basePath;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    BoilServiceImpl boilService;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    BoilMapper boilMapper;

    @Autowired
    private BoilsumdataMapper boilsumdataMapper;

    @Autowired
    private BoilcollectMapper boilcollectMapper;

    @Autowired
    private Boil_tagMapper boil_tagMapper;

    // 根据文章标题返回推荐的文章，Levenshtein Distance算法
    public Object recommend(String title, int length) {
        LambdaQueryWrapper<Boil> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Boil::getHot);
        List<Boil> boils = boilMapper.selectList(queryWrapper);
        String data[][] = new String[boils.size()][3];
        for (int i = 0; i < boils.size(); i++) {
            Boil boil = boils.get(i);
            String title1 = boil.getTitle();
            String id = String.valueOf(boil.getId());
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
        if (length > boils.size()) {
            String result[][] = new String[boils.size()][2];
            for (int i = 0; i < boils.size(); i++) {
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
            List<Boil> boils = boilMapper.selectList(null);
            for (Boil boil : boils) {
                Date create_time = (Date) dateFormat.parse(boil.getCreate_time());
                Integer views = boil.getViews();
                int differHour = getDifferHour(create_time, time);
                System.out.println(differHour);
                Double counthot = counthot(views, differHour);
                boil.setHot(counthot);
                boilMapper.updateById(boil);
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
    public Result addBoil(HttpServletRequest request) throws ParseException {
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
        String outline = request.getParameter("outline");
        String tag = request.getParameter("tag");
        String link = request.getParameter("link");
        String filename = request.getParameter("filename");
        // 添加博客使用标签次数start
        LambdaQueryWrapper<Boil_tag> queryWrapper = new LambdaQueryWrapper<>();
        LambdaUpdateWrapper<Boil_tag> objectLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        String[] split = tag.split(",");
        for (String s : split) {
            queryWrapper.eq(Boil_tag::getTagname, s);
            Boil_tag tag1 = boil_tagMapper.selectOne(queryWrapper);
            Integer sum = tag1.getSum();
            tag1.setSum(++sum);
            boil_tagMapper.updateById(tag1);
        }
        // 添加博客使用标签次数end
        Integer views = 0;
        // 获取时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(System.currentTimeMillis());
        // 创建实体类
        Boil boil = new Boil();
        boil.setUsername(username);
        boil.setNickname(nickname);
        boil.setProfile_photo(profile_photo);
        boil.setTitle(title);
        boil.setContent(content);
        boil.setOutline(outline);
        boil.setViews(views);
        boil.setTag(tag);
        boil.setLink(link);
        boil.setFilename(filename);
        boil.setCreate_time(date);
        boil.setUpdate_time(date);
        int insert = boilMapper.insert(boil);
        Map<String, Object> map = new HashMap<>();
        if (insert > 0) {
            return result.insertBlogok(map);
        } else {
            return result.insertBlogno(map);
        }
    }

    /**
     * 添加博客图片
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
    public Result getById(@PathVariable("id") Integer id) {
        LambdaQueryWrapper<Boil> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Boil::getId, id);
        Boil boil2 = boilMapper.selectOne(queryWrapper1);
        if (boil2 != null) {
            // 获取博客主要内容
            Boil boil = boilMapper.selectById(id);
            String title = boil.getTitle();
            // 获取博客标签
            // 获取访问量
            Integer views = boil.getViews();
            // 访问量
            ++views;
            // 修改数据库中的访问量数据
            boilService.updateviews(views, id);
            Boil boil1 = boilService.getById(id);
            Object recommend = recommend(title, 5);
            HashMap<String, Object> map = new HashMap<>();
            map.put("recommend", recommend);
            map.put("boildata", boil1);
            return new Result(1, "返回博客数据成功", map);
        }
        return new Result(1, "没有该博客", "");
    }

    // 修改博客--√
    @PutMapping("/modify/{id}")
    public Result modify(HttpServletRequest request, @PathVariable("id") Integer id) {
        Result result = new Result();
        Boil boil = boilService.getById(id);
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String outline = request.getParameter("outline");
        String filename = request.getParameter("filename");
        String tag = request.getParameter("tag");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(System.currentTimeMillis());
        boil.setTitle(title);
        boil.setContent(content);
        boil.setOutline(outline);
        boil.setTag(tag);
        boil.setUpdate_time(date);
        boilService.updateById(boil);
        return result.modifyok("");
    }

    // 删除博客--√
    @DeleteMapping({"/delete/{id}"})
    public Result delete(HttpServletRequest request, @PathVariable("id") Integer id) {
        Result result = new Result();
        LambdaQueryWrapper<Boil> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Boil::getId, id);
        Boil boil = boilMapper.selectOne(queryWrapper1);
        if (boil != null) {
            String filename = boil.getFilename();
            boilMapper.deleteById(id);
            LambdaQueryWrapper<Boilcollect> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Boilcollect::getBoil_id, id);
            List<Boilcollect> collects = boilcollectMapper.selectList(queryWrapper);
            List<Integer> idList = Arrays.asList();
            for (Boilcollect collect : collects) {
                boilcollectMapper.deleteById(collect.getId());
            }
            return new Result(1, "删除博客成功", "");
        }
        return new Result(1, "没有该博客", "");
    }

    // 博客入口页--分页显示所有博客的标题和简介--pc端--
    @GetMapping("/getallboil")
    public Result getallboil(Integer page, Integer pageSize, HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        Page<Boil> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Boil> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Boil::getHot);
        boilService.page(pageInfo, queryWrapper);
        return new Result(1, "查看数据成功", pageInfo);
    }

    // 博客入口页--懒加载显示所有博客的标题和简介--小程序端--√
    @GetMapping("/wxgetallboil")
    public Result wxgetallboil(HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        LambdaQueryWrapper<Boil> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(Boil::getId).orderByDesc(Boil::getHot);
        List<Boil> boils = boilMapper.selectList(queryWrapper);
        return new Result(1, "博客入口页加载成功", boils);
    }

    // 分页显示查看个人所有博客--pc端
    @GetMapping("/myallboil")
    public Result myallboil(Integer page, Integer pageSize, HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        Page<Boil> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Boil> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Boil::getUsername, username).orderByDesc(Boil::getHot);
        boilService.page(pageInfo, queryWrapper);
        return new Result(1, "查看数据成功", pageInfo);
    }

    // 懒加载显示查看个人所有博客--小程序端--√
    @GetMapping("/wxmyallboil")
    public Result wxmyallboil(HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        HashMap<String, Object> map = new HashMap<>();
        LambdaQueryWrapper<Boil> queryWrapper3 = new LambdaQueryWrapper<>();
        queryWrapper3.eq(Boil::getUsername, username);
        queryWrapper3.orderByDesc(Boil::getCreate_time);
        List<Boil> boilss = boilMapper.selectList(queryWrapper3);
        if (boilss.size() != 0) {
            // ----列表页上方信息
            // 总点赞数
            Integer sumgttu = 0;
            // 总访问量
            Integer sumviews = 0;
            LambdaQueryWrapper<Boilsumdata> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.eq(Boilsumdata::getUsername, username);
            Boilsumdata boilsumdata1 = boilsumdataMapper.selectOne(queryWrapper2);
            if (boilsumdata1 != null) {
                LambdaQueryWrapper<Boil> queryWrapper = new LambdaQueryWrapper<>();
                LambdaQueryWrapper<Boilcollect> queryWrapper1 = new LambdaQueryWrapper<>();
                queryWrapper.eq(Boil::getUsername, username);
                List<Boil> boils = boilMapper.selectList(queryWrapper);
                for (Boil boil : boils) {
                    Integer gttu = boil.getGttu();
                    Integer views = boil.getViews();
                    sumgttu += gttu;
                    sumviews += views;
                }
                queryWrapper1.eq(Boilcollect::getUsername, username);
                List<Boilcollect> collects = boilcollectMapper.selectList(queryWrapper1);
                int size = collects.size();
                boilsumdata1.setSumgttu(sumgttu);
                boilsumdata1.setSumviews(sumviews);
                boilsumdata1.setSumcollect(size);
                int i = boilsumdataMapper.updateById(boilsumdata1);
                map.put("upperdata", boilsumdata1);
                map.put("boildata", boilss);
                return new Result(1, "个人所有博客列表页加载成功", map);
            }
            // 头像数据
            String profile_photo = boilss.get(0).getProfile_photo();
            LambdaQueryWrapper<Boil> queryWrapper = new LambdaQueryWrapper<>();
            LambdaQueryWrapper<Boilcollect> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper.eq(Boil::getUsername, username);
            List<Boil> boils = boilMapper.selectList(queryWrapper);
            for (Boil boil : boils) {
                Integer gttu = boil.getGttu();
                Integer views = boil.getViews();
                sumgttu += gttu;
                sumviews += views;
            }
            queryWrapper1.eq(Boilcollect::getUsername, username);
            List<Boilcollect> collects = boilcollectMapper.selectList(queryWrapper1);
            int size = collects.size();
            Boilsumdata boilsumdata = new Boilsumdata();
            boilsumdata.setUsername(username);
            boilsumdata.setSumgttu(sumgttu);
            boilsumdata.setSumviews(sumviews);
            boilsumdata.setSumcollect(size);
            boilsumdata.setProfile_photo(profile_photo);
            int insert = boilsumdataMapper.insert(boilsumdata);
            map.put("upperdata", boilsumdata);
            map.put("boildata", boilss);
            return new Result(1, "个人所有博客列表页加载成功", map);
        }
        return new Result(1, "没有博客数据", "");
    }

    // 搜索博客-pc端
    @PostMapping("/search")
    public Result search(HttpServletRequest request, Integer page, Integer pageSize) {
        String search = request.getParameter("search");
        LambdaQueryWrapper<Boil> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Boil::getTitle, search);
        List<Boil> boils = boilMapper.selectList(queryWrapper);
        return new Result(1, "搜索成功", boils);
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
        LambdaQueryWrapper<Boil> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Boil::getTag, tag).orderByDesc(Boil::getHot);
        List<Boil> boils = boilMapper.selectList(queryWrapper);
        return new Result(1, "返回数据成功", boils);
    }
}
