package com.chen.Sign.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.chen.Sign.pojo.User;
import com.chen.Sign.mapper.UserMapper;
import com.chen.Sign.service.UserService;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
@SuppressWarnings({"all"})
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    UserMapper userMapper;


    public User selectUserById(int id) {
        return userMapper.selectUserById(id);
    }

    // 登录模块
/*    public boolean login(User user) {
        // 获取存储到user对象中的数据
        String username = user.getUsername();
        String password = user.getPassword();
*//*        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getName, name);
        User u1 = userMapper.selectOne(lambdaQueryWrapper);*//*
        User u1 = userMapper.selectUserByName(username);
        System.out.println(u1);
        // 和数据库的数据进行比对
        if (u1 == null) {
            return false;
        } else {
            if (u1.getPassword().equals(password)) {
                return true;
            } else {
                return false;
            }
        }
    }*/

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public User login(User user) {
        // 根据接收用户名密码查询数据库
        User user1 = userMapper.login(user);
        if (user1 != null) {
            return user1;
        }
        throw new RuntimeException("登录失败...");
    }


    // 检查注册模块
    public boolean registerCheck(String username) {
        // 获取存储到user对象中的数据
        User u1 = userMapper.selectUserByName(username);
        // 和数据库的数据进行比对
        if (u1 == null) {
            return true;
        } else {
            return false;
        }
    }

    // 检查忘记密码的时候 邮箱和用户名的匹配关系
    public String useremail(String username) {
        User u1 = userMapper.selectUserByName(username);
        String email = u1.getEmail();
        return email;
    }

    public boolean updatepassword(@Param("username") String username, @Param("password") String password) {
        int x = userMapper.updatePassword(username, password);
        if (x > 0) {
            return true;
        } else {
            return false;
        }
    }

    // 返回个人信息
    public User selectallinformation(String username) {
        User user = userMapper.selectallinformation(username);
        return user;
    }

    public User editinformation(@Param("username") String username) {
        User user = userMapper.editinformation(username);
        return user;
    }

    public User upload(@Param("username") String username) {
        User user = userMapper.selectUserByName(username);
        return user;
    }

    public boolean insertProfile_photo(@Param("username") String username, @Param("profile_photo") String Filepath) {
        int i = userMapper.insertProfile_photo(username, Filepath);
        if (i > 0) {
            return true;
        }
        return false;
    }
}











