package com.chen.Sign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.Sign.pojo.Email;
import com.chen.Sign.pojo.User;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailMapper extends BaseMapper<Email> {

    public User selectUserById(int id);

    public User selectUserByName(String username);

    public int insertCode(User user);
}
