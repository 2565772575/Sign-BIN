package com.chen.Sign.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chen.Sign.common.Result;
import com.chen.Sign.mapper.Boil_tagMapper;
import com.chen.Sign.pojo.Boil_tag;
import com.chen.Sign.service.impl.Boil_tagServiceImpl;
import com.chen.Sign.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 陈帅彬
 * @date 2023/4/29 11:40
 */
@RestController
@Slf4j
@SuppressWarnings({"all"})
@RequestMapping("/boiltag")
@CrossOrigin
@Component
public class Boil_tagController {

    @Autowired
    private Boil_tagMapper boil_tagMapper;

    @Autowired
    private Boil_tagServiceImpl boil_tagService;

    /**
     * 根据使用次数返回标签
     *
     * @param request
     * @return
     */
    @GetMapping
    public Result tag(HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        LambdaQueryWrapper<Boil_tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Boil_tag::getSum);
        List<Boil_tag> tags = boil_tagMapper.selectList(queryWrapper);
        return new Result(1, "根据热度返回标签成功", tags);
    }
}
