package com.ruoyi.ruoyilab.service;

import com.ruoyi.ruoyilab.vo.JournalVo;
import com.ruoyi.ruoyilab.vo.JournalaVo;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author 陈帅彬
 * @date 2023/5/10 16:24
 */

public interface JournalService {
    List<JournalaVo> selectjournal(String username);

    List<JournalVo> selectlist();

    int repair(String username,String starttime, String endtime) throws ParseException;


    List<Map<String,Object>> selecttimeall(String starttime,String endtime);

    int endjournal(String endtime,String username) throws ParseException;

    int deletejournal(Integer id) throws ParseException;
}
