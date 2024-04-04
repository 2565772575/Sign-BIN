package com.chen.Sign.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author 陈帅彬
 * @date 2023/3/13 19:54
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"all"})
public class Comment {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer blogid;

    private String username;

    private String nickname;

    private String profile_photo;

    private Integer likenum;

    private String content;

    private String parentid;

    private Timestamp createtime;

}
