package com.chen.Sign.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chen.Sign.common.Result;
import com.chen.Sign.mapper.TagMapper;
import com.chen.Sign.pojo.Tag;
import com.chen.Sign.service.impl.TagServiceImpl;
import com.chen.Sign.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

// 标签模块
@RestController
@Slf4j
@SuppressWarnings({"all"})
@RequestMapping("/tag")
public class TagController {

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private TagServiceImpl tagService;

    /*
    * 1：返回给前端根据热度进行排序的标签
    * 2：前端每添加一次标签都进行数据库标签的使用次数添加
    * 3：
    * */

    /**
     * 根据使用次数返回标签
     * @param request
     * @return
     */
    @GetMapping
    public Result tag(HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Tag::getSum);
        List<Tag> tags = tagMapper.selectList(queryWrapper);
        return new Result(1,"根据热度返回标签成功",tags);
    }
}
