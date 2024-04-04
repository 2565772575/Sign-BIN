package com.chen.Sign.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"all"})
public class Message {

    // 消息发送者
    private String username;

    // 聊天文本
    private String message;

    // 消息接收者
    private String tousername;

    // 发送时间
    private String createtime;

    // 消息状态 0：未读 1：已读
    private Integer status;
}
