package com.chen.Sign.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import java.util.Date;

@Data
@ApiModel("签到学习记录类")
@TableName("journal")
public class Journal {

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;;

    private String username;

    private String content;

    private String create_time;

    private String update_time;

    private Long sum_time;

    private Integer state;


}
