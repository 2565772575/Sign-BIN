package com.chen.Sign.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.Sign.pojo.Email;
public interface EmailService extends IService<Email> {

    boolean sendMail(String to,String subject,String text);
}
