package com.chen.Sign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.Sign.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@SuppressWarnings({"all"})
public interface UserMapper extends BaseMapper<User> {

    User login(User user);

    public User selectUserById(int id);

    public User selectUserByName(String username);

    public List<User> selectAll();

    public int updatePassword(@Param("username") String username, @Param("password") String password);

    // 返回个人信息
    public User selectallinformation(String username);

    // 编辑个人信息
    public User editinformation(String username);

    public int insertProfile_photo(@Param("username") String username, @Param("profile_photo") String Filepath);


}
