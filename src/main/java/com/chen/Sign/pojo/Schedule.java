package com.chen.Sign.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
@SuppressWarnings({"all"})
public class Schedule {

    @TableId(value = "id",type = IdType.AUTO)
    Integer id;

    String uid;

    String task;

    Integer status;

    String time;
}
