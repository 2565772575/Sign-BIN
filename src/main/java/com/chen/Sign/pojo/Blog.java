package com.chen.Sign.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

@SuppressWarnings({"all"})
@ApiModel("博客类")
@Data
public class Blog {

    @TableId(value = "id", type = IdType.AUTO)
    Integer id;

    String title;

    String username;

    String nickname;

    String profile_photo;

    String content;

    Integer views;

    Integer gttu;

    Integer comment_num;

    String filename;

    String tag;

    Double hot;

    String link;

    String create_time;

    String update_time;
}
