package com.chen.Sign.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author 陈帅彬
 * @date 2023/2/28 19:06
 */
@Data
@SuppressWarnings({"all"})
public class Collect {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;

    private Integer blog_id;
}
