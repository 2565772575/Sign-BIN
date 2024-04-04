package com.chen.Sign.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 陈帅彬
 * @date 2023/2/21 16:30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"all"})
@ApiModel("时长类")
public class Journal_time {

    @TableId(value = "id",type = IdType.AUTO)
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
