package com.chen.Sign.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chen.Sign.common.Result;
import com.chen.Sign.mapper.User_likeMapper;
import com.chen.Sign.pojo.Blog;
import com.chen.Sign.pojo.User_like;
import com.chen.Sign.service.impl.BlogServiceImpl;
import com.chen.Sign.service.impl.UserServiceImpl;
import com.chen.Sign.service.impl.User_likeServiceImpl;
import com.chen.Sign.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@CrossOrigin
@SuppressWarnings({"all"})
public class User_likeController {

    @Autowired
    User_likeServiceImpl user_likeService;

    @Autowired
    User_likeMapper user_likeMapper;
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    BlogServiceImpl blogService;

    @Autowired
    UserServiceImpl userService;

    // 点赞
    @PostMapping("/likeblog/{id}")
    public Result likeBlog(@PathVariable("id") Integer id, HttpServletRequest request) {
        // 获取点赞用户id
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String uid = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        Blog blog = blogService.getById(id);
        // 判断是否有该用户
        LambdaQueryWrapper<User_like> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(User_like::getUid,uid).eq(User_like::getBlog_id,id);
        User_like user_like1 = user_likeMapper.selectOne(objectLambdaQueryWrapper);
        if (user_like1 != null) {
            // 有该用户
            // 判断status状态码,是否已经点赞
            if (user_like1.getStatus() == 1) {
                // 状态码为1，取消点赞
                user_like1.setStatus(0);
                user_likeService.updateById(user_like1);
                // 博客的点赞数量-1
                Integer gttu = blog.getGttu();
                blog.setGttu(--gttu);
                blogService.updateById(blog);
                return new Result(2, "取消点赞", "");
            } else {
                // 状态码为0,点赞
                user_like1.setStatus(1);
                user_likeService.updateById(user_like1);
                // 博客的点赞数量+1
                Integer gttu = blog.getGttu();
                blog.setGttu(++gttu);
                blogService.updateById(blog);
                return new Result(1, "点赞成功", "");
            }
        } else {
            // 无用户
            // 添加数据，博客的点赞数量+1
            Integer gttu = blog.getGttu();
            blog.setGttu(++gttu);
            blogService.updateById(blog);
            // 添加数据到点赞表
            User_like user_like = new User_like();
            user_like.setBlog_id(id);
            user_like.setUid(Integer.parseInt(uid));
            user_like.setStatus(1);
            user_likeMapper.insert(user_like);
            return new Result(1, "点赞成功", "");
        }
    }
}

/*
 * 点赞的功能
 * 1：第一次点赞和后面的点赞  判断用户id是否存在，存在就不是第一次点赞，就判断status来进行决定点赞和取消点赞
 * 2：用户只能点击一次 判断该用户id是否在表中（在表中就判断status 如果为1,返回“已点赞”，如果为0，则可以点赞）（没在表中就创建）
 * 3：博客表中的点赞数量变化
 * */