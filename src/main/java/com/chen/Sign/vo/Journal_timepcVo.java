package com.chen.Sign.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.Sign.pojo.Journal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 陈帅彬
 * @date 2023/3/16 19:35
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"all"})
public class Journal_timepcVo {

    private Page<Journal> pageInfo;

    private String username;

    private String day_time;

    private String week_time;

    private String all_time;

    private String week_time_desc;

}
