package com.chen.Sign.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.sql.Timestamp;

@SuppressWarnings({"all"})
@Data
@ApiModel("用户类")
public class User {

    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("真实姓名")
    private String name;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("邮件")
    private String email;

    @ApiModelProperty("年级")
    private String grade;

    private String profile_photo;

    private String telephone;

    private String nickname;

    private String profession;

    private String sex;

    private String personalsignature;

    private Timestamp create_time;

    private Timestamp update_time;

    private Integer state;

}

