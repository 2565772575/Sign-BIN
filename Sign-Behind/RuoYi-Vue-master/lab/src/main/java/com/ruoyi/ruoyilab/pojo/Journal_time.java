package com.ruoyi.ruoyilab.pojo;



import lombok.Data;


/**
 * @author 陈帅彬
 * @date 2023/2/21 16:30
 */
@Data
@SuppressWarnings({"all"})
public class Journal_time {

    private Integer id;

    private String username;

    private String day_time;

    private String week_time;

    private String all_time;

    private String week_time_desc;

    private Integer continuous;

    private Integer addupday;

    private Integer status;


}
