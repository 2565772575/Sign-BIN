package com.ruoyi.ruoyilab.vo;

import lombok.Data;

/**
 * @author 陈帅彬
 * @date 2023/5/10 20:23
 */
@Data
public class JournalVo {

    private int id;

    private String name;

    private String sex;

    private String username;

    private String create_time;

    private String update_time;

    private String content;

    private Integer state;
}
