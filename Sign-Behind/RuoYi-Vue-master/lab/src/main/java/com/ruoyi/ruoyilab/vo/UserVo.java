package com.ruoyi.ruoyilab.vo;

import lombok.Data;

import java.sql.Timestamp;

/**
 * @author 陈帅彬
 * @date 2023/5/10 20:17
 */
@Data
public class UserVo {

    private int id;

    private String username;

    private String name;

    private String email;

    private String sex;

    private Integer state;

}
