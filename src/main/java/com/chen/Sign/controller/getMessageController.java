package com.chen.Sign.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chen.Sign.common.Result;
import com.chen.Sign.mapper.MessageMapper;
import com.chen.Sign.mapper.RelationMapper;
import com.chen.Sign.mapper.UserMapper;
import com.chen.Sign.pojo.Message;
import com.chen.Sign.pojo.Relation;
import com.chen.Sign.service.MessageService;
import com.chen.Sign.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 陈帅彬
 * @date 2023/3/21 19:30
 */
@RestController
@Slf4j
@SuppressWarnings({"all"})
@RequestMapping("/message")
@CrossOrigin
@Component
public class getMessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RelationMapper relationMapper;
    /**
     * 聊天列表返回所有消息
     * @return
     */
    @GetMapping()
    public Result getMessage(HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        LambdaQueryWrapper<Relation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Relation::getUsername,username).or().eq(Relation::getTousername,username);
        List<Message> messagess = new ArrayList<>();
        List<Relation> relations = relationMapper.selectList(queryWrapper);
        for (Relation relation : relations) {
            LambdaQueryWrapper<Message> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(Message::getUsername,relation.getUsername()).eq(Message::getTousername,relation.getTousername())
                    .or()
                    .eq(Message::getUsername,relation.getTousername()).eq(Message::getTousername,relation.getUsername())
                    .orderByDesc(Message::getCreatetime);
            List<Message> messages = messageMapper.selectList(queryWrapper1);
            if (messages != null) {
                Message message = messages.get(0);
                messagess.add(message);
            }
        }
        return new Result(1, "返回消息列表成功", messagess);
    }

    @DeleteMapping()
    public Result deleteMessage(HttpServletRequest request) {
        String tousername = request.getParameter("tousername");
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        LambdaQueryWrapper<Relation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Relation::getTousername,tousername).eq(Relation::getUsername,username);
        relationMapper.delete(queryWrapper);
        return new Result(1, "移除消息列表成功", "");
    }
}
