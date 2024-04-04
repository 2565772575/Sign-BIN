package com.chen.Sign.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chen.Sign.common.Result;
import com.chen.Sign.mapper.User_boilMapper;
import com.chen.Sign.pojo.Boil;
import com.chen.Sign.pojo.User_boil;
import com.chen.Sign.service.impl.BoilServiceImpl;
import com.chen.Sign.service.impl.UserServiceImpl;
import com.chen.Sign.service.impl.User_boilServiceImpl;
import com.chen.Sign.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 陈帅彬
 * @date 2023/4/29 11:41
 */
@RestController
@Slf4j
@SuppressWarnings({"all"})
@CrossOrigin
@Component
public class User_boilController {
    @Autowired
    User_boilServiceImpl user_boilService;

    @Autowired
    User_boilMapper user_boilMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    BoilServiceImpl boilService;

    @Autowired
    UserServiceImpl userService;

    // 点赞
    @PostMapping("/likeboil/{id}")
    public Result likeBoil(@PathVariable("id") Integer id, HttpServletRequest request) {
        // 获取点赞用户id
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String uid = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        Boil boil = boilService.getById(id);
        // 判断是否有该用户
        LambdaQueryWrapper<User_boil> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(User_boil::getUid,uid).eq(User_boil::getBoil_id,id);
        User_boil user_boil1 = user_boilMapper.selectOne(objectLambdaQueryWrapper);
        if (user_boil1 != null) {
            // 有该用户
            // 判断status状态码,是否已经点赞
            if (user_boil1.getStatus() == 1) {
                // 状态码为1，取消点赞
                user_boil1.setStatus(0);
                user_boilService.updateById(user_boil1);
                // 博客的点赞数量-1
                Integer gttu = boil.getGttu();
                boil.setGttu(--gttu);
                boilService.updateById(boil);
                return new Result(2, "取消点赞", "");
            } else {
                // 状态码为0,点赞
                user_boil1.setStatus(1);
                user_boilService.updateById(user_boil1);
                // 博客的点赞数量+1
                Integer gttu = boil.getGttu();
                boil.setGttu(++gttu);
                boilService.updateById(boil);
                return new Result(1, "点赞成功", "");
            }
        } else {
            // 无用户
            // 添加数据，博客的点赞数量+1
            Integer gttu = boil.getGttu();
            boil.setGttu(++gttu);
            boilService.updateById(boil);
            // 添加数据到点赞表
            User_boil user_boil = new User_boil();
            user_boil.setBoil_id(id);
            user_boil.setUid(Integer.parseInt(uid));
            user_boil.setStatus(1);
            user_boilMapper.insert(user_boil);
            return new Result(1, "点赞成功", "");
        }
    }


}
