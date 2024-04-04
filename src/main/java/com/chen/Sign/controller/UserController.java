package com.chen.Sign.controller;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.chen.Sign.common.GuiguException;
import com.chen.Sign.common.Result;
import com.chen.Sign.mapper.BlogMapper;
import com.chen.Sign.mapper.BlogsumdataMapper;
import com.chen.Sign.mapper.Journal_timeMapper;
import com.chen.Sign.pojo.Blog;
import com.chen.Sign.pojo.Blogsumdata;
import com.chen.Sign.pojo.Journal_time;
import com.chen.Sign.pojo.User;
import com.chen.Sign.mapper.UserMapper;
import com.chen.Sign.service.impl.EmailServiceImpl;
import com.chen.Sign.service.impl.UserServiceImpl;
import com.chen.Sign.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

// 用户模块
@Slf4j
@RestController
@CrossOrigin
@SuppressWarnings({"all"})
@RequestMapping("/user")
public class UserController {

    @Value("${lab.path}")
    private String basePath;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    EmailServiceImpl emailService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    Journal_timeMapper journal_timeMapper;

    @Autowired
    BlogsumdataMapper blogsumdataMapper;

    // 随机生成4位验证码
    public static String getNumber() {
        String str = "123456789qwertyuiopoasdfghjklzxcvbnm";
        String code = "";
        for (int i = 0; i < 4; i++) {
            int index = (int) (Math.random() * str.length());
            code += str.charAt(index);
        }
        return code;
    }


    // JWT登录
    @PostMapping("/login")
    public Result login(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        // 获取前端的username和password
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        User user = new User();
        user.setUsername(username);
        user.setPassword(DigestUtils.md5Hex(password));
        Map<String, Object> map = new HashMap<>();
        try {
            User login = userService.login(user);
            HashMap<String, String> payload = new HashMap<>();
            payload.put("id", String.valueOf(login.getId()));
            System.out.println(String.valueOf(login.getId()));
            payload.put("username", login.getUsername());
            // 生成JWT的令牌
            String token = JWTUtils.getToken(payload);
            System.out.println(token);
            map.put("token", token);
            sign(String.valueOf(login.getId()));
            return new Result(1, "认证成功", map);
        } catch (Exception e) {
            map.put("msg", e.getMessage());
            return new Result(0, "认证失败", map);
        }
    }

    // 注册
    @PostMapping("/register")
    public Result register(HttpServletRequest request, HttpServletResponse response) {
        Result result = new Result();
        String username = request.getParameter("username");
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String grade2[] = request.getParameterValues("grade");
        String grade1 = Arrays.toString(grade2);
        String grade = grade1.substring(1, grade1.length() - 2);
        if (username==null || name == null || password==null || email==null || grade==null) {
            throw new GuiguException(0, "有必需参数未填写", "");
        }
        boolean usercheck = userService.registerCheck(name);
        Map<String, String> map = new HashMap<>();
        if (usercheck) {
            String code1 = request.getParameter("code");
            String code = (String) redisTemplate.opsForValue().get(code1);
            System.out.println(code1);
            System.out.println(code);
            if (code.equals(code1)) {
                User user = new User();
                user.setUsername(username);
                user.setName(name);
                user.setPassword(DigestUtils.md5Hex(password));
                user.setEmail(email);
                user.setGrade(grade);
                int i = userMapper.insert(user);
                if (i > 0) {
                    redisTemplate.delete(code);
                    Journal_time journal_time = new Journal_time();
                    journal_time.setUsername(username);
                    journal_time.setDay_time("0小时0分钟");
                    journal_time.setWeek_time("0小时0分钟");
                    journal_time.setAll_time("0小时0分钟");
                    journal_time.setWeek_time_desc("第0名");
                    journal_time.setContinuous(0);
                    journal_time.setStatus(0);
                    journal_time.setAddupday(0);
                    int insert = journal_timeMapper.insert(journal_time);
                    Blogsumdata blogsumdata = new Blogsumdata();
                    blogsumdata.setUsername(username);
                    blogsumdata.setSumcollect(0);
                    blogsumdata.setSumviews(0);
                    blogsumdata.setSumgttu(0);
                    blogsumdataMapper.insert(blogsumdata);
                    return result.registerok(map);
                } else {
                    return result.registerno(map);
                }
            } else {
                // 验证码错误    code 3
                return result.checkcodeno(map);
            }
        } else {
            // 用户名已经存在 code 2
            map.put("username", username);
            return result.registerusernamecheckno(map);
        }
    }

    // 忘记密码
    @PostMapping("/forgetpassword")
    public Result forgotpassword(HttpServletRequest request, HttpServletResponse response) {
        Result result = new Result();
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String passwordAgain = request.getParameter("passwordAgain");
        boolean usercheck = userService.registerCheck(username);
        HashMap<String, String> map = new HashMap<>();
        if (usercheck) {
            // 数据库中没有该用户名的数据 code 2
            map.put("username", username);
            return Result.forgotusernamenoexist(map);
        } else {
            // 数据库中有该用户名的数据
            String code1 = request.getParameter("code");
            String code = (String) redisTemplate.opsForValue().get(code1);
            if (code.equals(code1)) {
                boolean flag = userService.updatepassword(username, DigestUtils.md5Hex(password));
                if (flag) {
                    // 修改成功 code 1
                    return result.updatepasswordok(map);
                } else {
                    // 修改失败 code 0
                    return result.updatepasswordno(map);
                }
            } else {
                // 验证码错误    code 3
                return result.checkcodeno(map);
            }
        }
    }

    //邮件发送
    @PostMapping("/mail")
    @ResponseBody
    public Result mail(HttpServletRequest request) {
        Result result = new Result();
        String code = getNumber();
        String email = request.getParameter("email");
        redisTemplate.opsForValue().set(code, code, Duration.ofMinutes(10L));
        boolean b = emailService.sendMail(email, "实验室注册验证码,有效时间为10分钟:", code);
        // 邮件发送成功
        HashMap<String, String> map = new HashMap<>();
        result.setCode(200);
        return result.mailok(map);
    }

    //忘记密码邮箱验证
    @PostMapping("/forgetmail")
    public Result forgetmail(HttpServletRequest request) {
        Result result = new Result();
        String code = getNumber();
        String email = request.getParameter("email");
        String username = request.getParameter("username");
        String useremail = userService.useremail(username);
        System.out.println(email);
        System.out.println(username);
        System.out.println(useremail);
        HashMap<String, String> map = new HashMap<>();
        if (useremail.equals(email)) {
            redisTemplate.opsForValue().set(code, code, Duration.ofMinutes(10L));
            boolean b = emailService.sendMail(email, "实验室注册验证码,有效时间为10分钟:", code);
            // 邮件发送成功
            result.setCode(200);
            return result.mailok(map);
        } else {
            // 邮箱错误     code 2
            return Result.emailnomatch(map);
        }
    }

    // 退出登录
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        Result result = new Result();
        // 清理Session中保存的当前登录的用户名
        String token = request.getHeader("token");
        redisTemplate.delete(token);
        Map<String, String> map = new HashMap<>();
        return result.logoutok(map);
    }

    // 返回个人信息
    @GetMapping("/allinformation")
    public Result allinformation(HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        Integer id = tokenInfo.getClaim("id").asInt();
        String username = tokenInfo.getClaim("username").asString();
        Result result = new Result();
        User user = userService.selectallinformation(username);
        Map<String, String> map = new HashMap<>();
        map.put("username", user.getUsername());
        map.put("name", user.getName());
        map.put("nickname", user.getNickname());
        map.put("sex", user.getSex());
        map.put("personalsignature", user.getPersonalsignature());
        map.put("email", user.getEmail());
        map.put("telephone", user.getTelephone());
        map.put("grade", user.getGrade());
        map.put("profession", user.getProfession());
        return result.allinformationok(map);
    }

    // 编辑个人信息
    @PutMapping("/editinformation")
    public Result editinformation(HttpServletRequest request) {
        Result result = new Result();
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        Integer id = tokenInfo.getClaim("id").asInt();
        String username = tokenInfo.getClaim("username").asString();
        String name = request.getParameter("name");
        String nickname = request.getParameter("nickname");
        String sex2[] = request.getParameterValues("sex");
        String sex1 = Arrays.toString(sex2);
        System.out.println("性别：--------------------------->"+sex1);
        String sex = sex1.substring(1, sex1.length() - 1);
        System.out.println(sex);
        String personalsignature = request.getParameter("personalsignature");
        String email = request.getParameter("email");
        String telephone = request.getParameter("telephone");
        String grade2[] = request.getParameterValues("grade");
        String grade1 = Arrays.toString(grade2);
        String grade = grade1.substring(1, grade1.length() - 2);
        String profession = request.getParameter("profession");
        if (username==null || name == null  || email==null || grade==null) {
            throw new GuiguException(0, "有必需参数未填写", "");
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername,username);
        User user = userMapper.selectOne(queryWrapper);
        user.setName(name);
        user.setNickname(nickname);
        user.setSex(sex);
        user.setPersonalsignature(personalsignature);
        user.setEmail(email);
        user.setTelephone(telephone);
        user.setGrade(grade);
        user.setProfession(profession);
        int i = userMapper.updateById(user);
        if (i > 0) {
            return result.editinformationok("");
        } else {
            return result.editinformationno("");
        }
    }

    // 上传/修改头像文件名保存到数据库
    @PostMapping("/uploadpp")
    public Result upload(@RequestPart("file") MultipartFile file, HttpServletRequest request) throws IOException {
        // 上传头像文件
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        // 检测是否有头像
        User user = userMapper.selectUserById(Integer.parseInt(id));
        if (user.getProfile_photo()!= null) {
            // 删除原来头像的文件
            String profile_photo = user.getProfile_photo();
            String[] split = profile_photo.split("/");
            String s = split[2];
            String filepath = basePath + s;
            File file1 = new File(filepath);
            if (file1.exists()) {
                file1.delete();
            }
        }
        // 文件开始上传
        log.info(file.toString());
        long starttime = System.currentTimeMillis();
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
        // 文件传输完成
        // user表修改头像数据
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("username",username).set("profile_photo",address);
        int update = userMapper.update(null, updateWrapper);
        // blogsumdata修改头像数据
        UpdateWrapper<Blogsumdata> updateWrapper1 = new UpdateWrapper<>();
        updateWrapper1.eq("username",username).set("profile_photo",address);
        int update1 = blogsumdataMapper.update(null, updateWrapper1);
        // blog修改头像数据
        UpdateWrapper<Blog> updateWrapper2 = new UpdateWrapper<>();
        updateWrapper2.eq("username",username).set("profile_photo",address);
        int update2 = blogMapper.update(null, updateWrapper2);
        return new Result(1,"上传成功",address);
    }

    /**
     * 返回头像文件名
     * @param name
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping("/getpp")
    @ResponseBody
    public Result getImage(HttpServletRequest request)  {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername,username);
        User user = userMapper.selectOne(queryWrapper);
        return new Result(1,"返回头像名成功",user.getProfile_photo());
    }

    @PostMapping("/search")
    public Result searchuser(HttpServletRequest request) {
        String search = request.getParameter("search");
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(User::getNickname, search);
        List<User> users = userMapper.selectList(queryWrapper);
        return new Result(1, "返回数据成功", users);
    }

    //连续打卡天数
    @GetMapping("/signcount")
    public Result signCount(HttpServletRequest request) {
        // 1.获取当前登录用户
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String userId = tokenInfo.getClaim("id").asString();
        // 2.获取日期
        LocalDateTime now = LocalDateTime.now();
        // 3.拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = "sign:" + userId + keySuffix;
        // 4.获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        // 5.获取本月截止今天为止的所有签到记录，返回的是一个十进制的数字 bitfield sign:5:202203 GET U14 0
        List<Long> result = redisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );
        if (result == null || result.isEmpty()) {
            // 没有任何签到结果
            return new Result(0,"无签到结果","");
        }
        Long num = result.get(0);
        if (num == null || num == 0) {
            return new Result(0,"无签到结果","");
        }
        // 6.循环遍历
        int count = 0;
        while (true) {
            // 6.1.让这个数字与1做与运算，得到数字的最后一个bit位 // 判断这个bit位是否为0
            if ((num & 1) == 0) {
                // 如果为0，说明未签到，结束
                break;
            } else {
                // 如果不为0，说明已签到，计数器+1
                count++;
            }
            // 把数字右移一位，抛弃最后一个bit位，继续下一个bit位
            num >>>= 1;
        }
        return new Result(1,"签到天数",count);
    }

    // 打卡
    public void sign(String id) {
        // 2.获取日期
        LocalDateTime now = LocalDateTime.now();
        // 3.拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = "sign:" + id + keySuffix;
        // 4.获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        // 5.写入Redis SETBIT key offset 1
        redisTemplate.opsForValue().setBit(key,dayOfMonth - 1,true);
    }


}





