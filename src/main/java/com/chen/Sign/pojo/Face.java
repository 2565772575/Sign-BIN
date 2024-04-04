package com.chen.Sign.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 陈帅彬
 * @date 2023/3/22 20:19
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"all"})
public class Face {

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    private String username;

    private String userPhoto;

}
