package com.chen.Sign.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.Sign.mapper.MessageMapper;
import com.chen.Sign.pojo.Message;
import com.chen.Sign.service.MessageService;
import org.springframework.stereotype.Service;

/**
 * @author 陈帅彬
 * @date 2023/3/7 19:33
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

}
