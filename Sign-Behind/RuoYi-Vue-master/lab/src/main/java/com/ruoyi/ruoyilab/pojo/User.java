package com.ruoyi.ruoyilab.pojo;


import lombok.Data;

import java.sql.Timestamp;

@SuppressWarnings({"all"})
@Data
public class User {

    private int id;

    private String username;

    private String name;

    private String password;

    private String email;

    private String grade;

    private String profile_photo;

    private String telephone;

    private String nickname;

    private String profession;

    private String sex;

    private String personalsignature;

    private Timestamp create_time;

    private Timestamp update_time;

}

