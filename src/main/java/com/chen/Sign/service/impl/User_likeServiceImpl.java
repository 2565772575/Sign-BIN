package com.chen.Sign.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.Sign.mapper.User_likeMapper;
import com.chen.Sign.pojo.User_like;
import com.chen.Sign.service.User_likeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings({"all"})
public class User_likeServiceImpl extends ServiceImpl<User_likeMapper, User_like> implements User_likeService {

    @Autowired
    User_likeService user_likeService;

    @Autowired
    User_likeMapper user_likeMapper;


}
