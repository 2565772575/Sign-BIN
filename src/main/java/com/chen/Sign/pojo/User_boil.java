package com.chen.Sign.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author 陈帅彬
 * @date 2023/4/29 10:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"all"})
public class User_boil {

    @TableId(value = "id",type = IdType.AUTO)
    Integer id;

    int boil_id;

    int uid;

    int status;

    Timestamp create_time;

    Timestamp update_time;

}
