package com.chen.Sign.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chen.Sign.common.Result;
import com.chen.Sign.pojo.*;
import com.chen.Sign.mapper.BoilMapper;
import com.chen.Sign.mapper.BoilcollectMapper;
import com.chen.Sign.mapper.BoilsumdataMapper;
import com.chen.Sign.mapper.UserMapper;
import com.chen.Sign.service.BoilService;
import com.chen.Sign.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author 陈帅彬
 * @date 2023/4/29 11:40
 */
@RestController
@Slf4j
@SuppressWarnings({"all"})
@RequestMapping("/boilcollect")
@CrossOrigin
@Component
public class BoilcollectController {

    @Autowired
    private BoilcollectMapper boilcollectMapper;

    @Autowired
    private BoilMapper boilMapper;

    @Autowired
    private BoilsumdataMapper boilsumdataMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BoilService boilService;

    // 收藏博客
    @PostMapping("/{id}")
    public Result collect(HttpServletRequest request, @PathVariable("id") Integer id) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String username = tokenInfo.getClaim("username").asString();
        Boil boil = boilMapper.selectById(id);
        String username1 = boil.getUsername();
        LambdaQueryWrapper<Boilsumdata> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Boilsumdata::getUsername, username1);
        Boilsumdata boilsumdata = boilsumdataMapper.selectOne(queryWrapper1);
        LambdaQueryWrapper<Boilcollect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Boilcollect::getBoil_id, id);
        queryWrapper.eq(Boilcollect::getUsername, username);
        Boilcollect collect1 = boilcollectMapper.selectOne(queryWrapper);
        if (collect1 != null) {
            Integer id1 = collect1.getId();
            int i = boilcollectMapper.deleteById(id1);
            boilsumdata.setSumcollect(boilsumdata.getSumcollect() - 1);
            boilsumdataMapper.updateById(boilsumdata);
            return new Result(2, "取消收藏成功", "");
        }
        boilsumdata.setSumcollect(boilsumdata.getSumcollect() + 1);
        boilsumdataMapper.updateById(boilsumdata);
        Boilcollect boilcollect = new Boilcollect();
        boilcollect.setUsername(username);
        boilcollect.setBoil_id(id);
        int insert = boilcollectMapper.insert(boilcollect);
        if (insert > 0) {
            return new Result(1, "收藏成功", "");
        }
        return new Result(0, "收藏失败", "");
    }

    // 查看收藏博客--小程序
    @GetMapping("/wxfavorite")
    public Result wxfavorite(HttpServletRequest request) {
        int sum = 0;
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String username = tokenInfo.getClaim("username").asString();
        LambdaQueryWrapper<Boilcollect> queryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Boil> queryWrapper1 = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<User> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper.eq(Boilcollect::getUsername, username);
        List<Boilcollect> collects = boilcollectMapper.selectList(queryWrapper);
        List<Boil> list = new ArrayList<>();
        for (Boilcollect collect : collects) {
            Integer boil_id = collect.getBoil_id();
            queryWrapper1.eq(Boil::getId, boil_id);
            Boil boil = boilMapper.selectOne(queryWrapper1);
            list.add(boil);
            sum++;
        }
        queryWrapper2.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper2);
        HashMap<Object, Object> map = new HashMap<>();
        map.put("boildata", list);
        map.put("collectsum", sum);
        map.put("username", user.getUsername());
        map.put("profile_photo", user.getProfile_photo());
        map.put("nickname", user.getNickname());
        return new Result(1, "收藏博客数据返回成功", map);
    }
}
