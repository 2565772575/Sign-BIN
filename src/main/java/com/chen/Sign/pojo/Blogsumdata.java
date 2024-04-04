package com.chen.Sign.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author 陈帅彬
 * @date 2023/3/8 10:33
 */
@Data
@SuppressWarnings({"all"})
public class Blogsumdata {

    @TableId(type = IdType.AUTO)
    Integer id;

    String username;

    Integer sumgttu;

    Integer sumcollect;

    Integer sumviews;

    String profile_photo;

}
