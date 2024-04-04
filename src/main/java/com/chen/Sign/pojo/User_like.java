package com.chen.Sign.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.sql.Timestamp;

@SuppressWarnings({"all"})
@Data
public class User_like {

    @TableId(value = "id",type = IdType.AUTO)
    Integer id;

    int blog_id;

    int uid;

    int status;

    Timestamp create_time;

    Timestamp update_time;
}
