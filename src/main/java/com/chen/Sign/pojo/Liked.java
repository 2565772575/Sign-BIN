package com.chen.Sign.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 陈帅彬
 * @date 2023/3/10 20:14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"all"})
public class Liked {

    private Integer id;

    private Integer commentid;

    private String likeid;

    private Integer status;
}
