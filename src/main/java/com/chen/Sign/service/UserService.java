package com.chen.Sign.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.Sign.pojo.User;

import java.util.List;


public interface UserService extends IService<User> {

    User login(User user); // 登录接口


}
