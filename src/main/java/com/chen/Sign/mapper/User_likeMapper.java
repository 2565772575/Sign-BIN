package com.chen.Sign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.Sign.pojo.User_like;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings({"all"})
public interface User_likeMapper extends BaseMapper<User_like> {


    User_like selectgttustate(@Param("uid") Integer uid,@Param("id") Integer id);
}
