package com.chen.Sign.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chen.Sign.common.Result;
import com.chen.Sign.mapper.MessageMapper;
import com.chen.Sign.mapper.RelationMapper;
import com.chen.Sign.mapper.UserMapper;
import com.chen.Sign.pojo.Message;
import com.chen.Sign.pojo.Relation;
import com.chen.Sign.pojo.User;
import com.chen.Sign.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 陈帅彬
 * @date 2023/3/7 19:50
 * 聊天模块
 */
@Slf4j
@Component
@SuppressWarnings({"all"})
@ServerEndpoint("/websocket/{username}/{tousername}")
public class MessageController {
    @Autowired
    private static MessageService messageService;

    /**
     *  解决socket无法注入bean的问题
     */
    private static UserMapper userMapper;


    @Autowired
    private static RelationMapper relationMapper;

    @Autowired
    private  static MessageMapper messageMapper;

    @Autowired
    public void setChatService(UserMapper userMapper,MessageService messageService,RelationMapper relationMapper,MessageMapper messageMapper) {
        MessageController.messageService = messageService;
        MessageController.userMapper= userMapper;
        MessageController.relationMapper = relationMapper;
        MessageController.messageMapper = messageMapper;
    }
    /**
     * 设置一次性存储数据的list的长度为固定值，每当list的长度达到固定值时，向数据库存储一次
     */
    private static final Integer LIST_SIZE = 1;

    /**
     * 新建list集合存储数据
     */
    private static ArrayList<Message> MessageList = new ArrayList<>();

    /**
     * map(username,websocket)作为对象添加到集合中
     */
    private static Map<String, MessageController> clients = new ConcurrentHashMap<String, MessageController>();

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
    public void onOpen(@PathParam("username") String username, @PathParam("tousername") String tousername,Session session) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        List<User> users = userMapper.selectList(null);
        //把新用户名赋给变量
        this.username = username;
        //把新用户的 session 信息赋给变量
        this.session = session;
        //输出 websocket 信息
        log.info("现在来连接的客户id：" + session.getId() + "用户名：" + username);
        // 将message的状态值进行修改
        // 1.取出数据库中tousername是username,数据库中username是tousername状态值为0的数据,修改状态值
        int i = messageMapper.updatestatus(username,tousername);
        // 对双方消息进行返回
        queryWrapper.eq(Message::getUsername,username).eq(Message::getTousername,tousername).or().eq(Message::getUsername,tousername).eq(Message::getTousername,username);
        queryWrapper.orderByAsc(Message::getCreatetime);
        List<Message> messages = messageMapper.selectList(queryWrapper);
        // 头像地址开始
        User user = userMapper.selectUserByName(username);
        User user1 = userMapper.selectUserByName(tousername);
        String profile_photo = user.getProfile_photo();
        String profile_photo1 = user1.getProfile_photo();
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(username,profile_photo1);
        hashMap.put(tousername,profile_photo1);
        // 头像地址结束

        HashMap<String, Object> map = new HashMap<>();
        map.put("messages",JSON.toJSON(messages));
        map.put("hashmap",JSON.toJSON(hashMap));
        Result result = new Result(1,"发送消息成功",JSON.toJSON(map));
        try {
            //把自己的信息加入到map当中去，this=当前类（把当前类作为对象保存起来）
            clients.put(username, this);
            // 消息发送给客户端
            sendMessageAll(String.valueOf(JSON.toJSON(result)));
            log.info(String.valueOf(JSON.toJSON(result)));
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
        //关闭连接前，判断list集合是否有数据，如果有，批量保存到数据库
        if (MessageList.size() < LIST_SIZE) {
            messageService.saveBatch(MessageList);
        }
        log.info("有连接关闭！");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.info("服务端发生了错误" + error.getMessage());
    }

    /**
     * 监听消息（收到客户端的消息立即执行）
     *
     * @param message 消息
     * @param session 会话
     */
    @OnMessage
    public void onMessage(String message, Session session,@PathParam("username") String username6, @PathParam("tousername") String tousername6) throws IOException {
        try {
            log.info("来自客户端消息：" + message + "客户端的id是：" + session.getId());
            //用户发送的信息
            com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(message);
            //发送的内容
            String textMessage = jsonObject.getString("message");
            //发送人
            String fromusername = jsonObject.getString("username");
            //接收人  to=all 发送消息给所有人 || to= !all   to == 用户名
            String tousername = jsonObject.getString("to");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = sdf.format(System.currentTimeMillis());
            //新建message对象
            Message message1 = new Message();
            //设置发送者的username
            message1.setUsername(fromusername);
            //设置发送的信息
            message1.setMessage(textMessage);
            //判断接收者
            message1.setTousername(tousername);
            message1.setCreatetime(date);
            message1.setStatus(0);
            // 添加关系到数据库
            LambdaQueryWrapper<Relation> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(Relation::getUsername,username).eq(Relation::getTousername,tousername).or().eq(Relation::getTousername,username).eq(Relation::getUsername,tousername);
            List<Relation> relations = relationMapper.selectList(queryWrapper1);
            if (relations.size() == 0) {
                Relation relation = new Relation();
                relation.setUsername(fromusername);
                relation.setTousername(tousername);
                relationMapper.insert(relation);
            }
            //批量保存信息
            //将每条记录添加到list集合中
            MessageList.add(message1);
            //判断list集合长度
            if (MessageList.size() == LIST_SIZE) {
                messageService.saveBatch(MessageList);
                //清空集合
                MessageList.clear();
            }
            Map<String, Object> map1 = new HashMap(100);
            map1.put("textMessage", textMessage);
            map1.put("fromusername", fromusername);
            //消息发送指定人（同步）
            map1.put("tousername", tousername);
            LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Message::getUsername,username).eq(Message::getTousername,tousername).or().eq(Message::getUsername,tousername).eq(Message::getTousername,username);
            queryWrapper.orderByAsc(Message::getCreatetime);
            List<Message> messages = messageMapper.selectList(queryWrapper);
            HashMap<String, Object> map = new HashMap<>();
            map.put("messages",JSON.toJSON(messages));
            map.put("hashmap",JSON.toJSON(""));
            Result result = new Result(1,"发送消息成功",JSON.toJSON(map));
            sendMessageTo(String.valueOf(JSON.toJSON(map1)), tousername);
            sendMessageAll(String.valueOf(JSON.toJSON(result)));
            log.info(String.valueOf(JSON.toJSON(result)));
        } catch (Exception e) {
            log.info("发生了错误了" + e);
        }
    }



    /**
     * 消息发送指定人
     */
    public void sendMessageTo(String message, String toUserName) throws IOException {
        //遍历所有用户
        for (MessageController item : clients.values()) {
            if (item.username.equals(toUserName)) {
                //消息发送指定人（同步）
                item.session.getBasicRemote().sendText(message);
                break;
            }
        }
    }

    /**
     * 消息发送所有人
     */
    public void sendMessageAll(String message) throws IOException {
        for (MessageController item : clients.values()) {
            //消息发送所有人（同步）getAsyncRemote
            item.session.getBasicRemote().sendText(message);
        }
    }

}