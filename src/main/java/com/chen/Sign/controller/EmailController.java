package com.chen.Sign.controller;

import com.chen.Sign.service.impl.EmailServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

// 邮件模块
@RestController
@Slf4j
@SuppressWarnings({"all"})
@CrossOrigin
public class EmailController {

    @Autowired
    EmailServiceImpl emailService;

    // 随机生成4位验证码
    public static String getNumber() {
        String str = "123456789qwertyuiopoasdfghjklzxcvbnm";
        String code = "";
        for (int i = 0; i < 4; i++) {
            int index = (int) (Math.random() * str.length());
            code += str.charAt(index);
        }
        return code;
    }
}
