package com.chen.Sign.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 陈帅彬
 * @date 2023/4/29 10:55
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"all"})
public class Boil_tag {
    @TableId(value = "id", type = IdType.AUTO)
    Integer id;

    String tagname;

    Integer sum;
}
