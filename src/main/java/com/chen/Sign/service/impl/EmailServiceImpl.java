package com.chen.Sign.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.Sign.pojo.Email;
import com.chen.Sign.pojo.User;
import com.chen.Sign.mapper.EmailMapper;
import com.chen.Sign.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
@SuppressWarnings({"all"})
public class EmailServiceImpl extends ServiceImpl<EmailMapper, Email> implements EmailService {

    private final JavaMailSender mailSender;

    private String from;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Autowired
    EmailMapper emailMapper;



    @Override
    public boolean sendMail(String to, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("2565772575@qq.com");

        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        try {
            mailSender.send(msg);
        } catch (MailException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
        return true;
    }

    public User SearchIdEmail(int id){
        User user = emailMapper.selectUserById(id);
        return user;
    }

    public User SearchNameEmail(String username){
        User user = emailMapper.selectUserByName(username);
        return user;
    }

    public boolean InsertCodeEmail(User user){
        int x = emailMapper.insertCode(user);
        if(x > 0){
            return true;
        } else {
            return false;
        }
    }
}
