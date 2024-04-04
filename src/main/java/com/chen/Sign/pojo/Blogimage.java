package com.chen.Sign.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author 陈帅彬
 * @date 2023/4/17 15:11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"all"})
public class Blogimage {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer blogid;

    private String address;

    private Timestamp createtime;

    private Timestamp updatetime;

}
