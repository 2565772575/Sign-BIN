package com.chen.Sign.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
@SuppressWarnings({"all"})
public class Tag {

    @TableId(value = "id", type = IdType.AUTO)
    Integer id;

    String tagname;

    Integer sum;
}
