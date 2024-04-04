package com.chen.Sign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.Sign.pojo.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 陈帅彬
 * @date 2023/3/7 19:29
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {


    public List<Message> selectMessage(@Param("username") String username);


    int updatestatus(@Param("username") String username,@Param("tousername") String tousername);
}
