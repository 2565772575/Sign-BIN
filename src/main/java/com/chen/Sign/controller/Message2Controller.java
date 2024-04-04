package com.chen.Sign.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chen.Sign.mapper.MessageMapper;
import com.chen.Sign.mapper.UserMapper;
import com.chen.Sign.pojo.Message;
import com.chen.Sign.pojo.User;
import com.chen.Sign.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 陈帅彬
 * @date 2023/3/26 20:08
 */
@Slf4j
@Component
@SuppressWarnings({"all"})
@ServerEndpoint("/websocket2/{username}")
public class Message2Controller {
    @Autowired
    private static MessageService messageService;

    /**
     * 解决socket无法注入bean的问题
     */
    @Autowired
    private static UserMapper userMapper;

    @Autowired
    private static MessageMapper messageMapper;

    @Autowired
    public void setChatService(UserMapper userMapper, MessageService messageService, MessageMapper messageMapper) {
        Message2Controller.messageService = messageService;
        Message2Controller.userMapper = userMapper;
        Message2Controller.messageMapper = messageMapper;
    }

    /**
     * map(username,websocket)作为对象添加到集合中
     */
    private static Map<String, Message2Controller> clients = new ConcurrentHashMap<String, Message2Controller>();

    /**
     * session会话
     */
    private Session session;
    /**
     * 用户名称
     */
    private String username;

    /**
     * 监听连接（有用户连接，立马到来执行这个方法）
     * session 发生变化
     *
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("username") String username, Session session) {
        int sum = 0;
        List<User> users = userMapper.selectList(null);
        //把新用户名赋给变量
        this.username = username;
        // 根据用户查询id
        User user = userMapper.selectUserByName(username);
        int id = user.getId();
        //把新用户的 session 信息赋给变量
        this.session = session;
        //输出 websocket 信息
        log.info("现在来连接的客户id：" + session.getId() + "用户名：" + username);
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getTousername, username).eq(Message::getStatus, 0);
        List<Message> messages = messageMapper.selectList(queryWrapper);
        for (Message message : messages) {
            sum++;
        }
        try {
            clients.put(username, this);
            //获得所有在线的用户lists
            Set<String> lists = clients.keySet();
            Map<String, Object> map = new HashMap(100);
            //把所有用户放入map
            map.put("allUser", JSON.toJSON(users));
            //把所有在线用户放入map
            map.put("onlineUsers", JSON.toJSON(lists));
            // 把消息未读条数放入map
            map.put("unreadsum", sum);
            sendMessageAll(String.valueOf(JSON.toJSON(map)));
        } catch (IOException e) {
            log.info("发生了错误");
        }
    }


    /**
     * 监听连接断开（有用户退出，会立马到来执行这个方法）
     */
    @OnClose
    public void onClose() {
        //从所有在线用户的map中去除下线用户
        clients.remove(username);
        log.info("有连接关闭！");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.info("服务端发生了错误" + error.getMessage());
    }


    /**
     * 消息发送所有人
     */
    public void sendMessageAll(String message) throws IOException {
        for (Message2Controller item : clients.values()) {
            //消息发送所有人（同步）getAsyncRemote
            item.session.getBasicRemote().sendText(message);
        }
    }
}
