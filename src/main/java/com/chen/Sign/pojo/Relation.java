package com.chen.Sign.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author 陈帅彬
 * @date 2023/3/26 20:14
 */
@Data
@SuppressWarnings({"all"})
public class Relation {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;

    private String tousername;
}
