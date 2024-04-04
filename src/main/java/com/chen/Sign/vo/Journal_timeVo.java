package com.chen.Sign.vo;

import com.chen.Sign.pojo.Journal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 陈帅彬
 * @date 2023/2/26 9:52
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"all"})
public class Journal_timeVo {

    private List<Journal> journalList;

    private String username;

    private String day_time;

    private String week_time;

    private String all_time;

    private String week_time_desc;

    private Integer state;

}
