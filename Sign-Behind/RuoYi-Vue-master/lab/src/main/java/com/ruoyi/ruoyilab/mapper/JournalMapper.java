package com.ruoyi.ruoyilab.mapper;

import com.ruoyi.ruoyilab.pojo.Journal;
import com.ruoyi.ruoyilab.pojo.Journal_time;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author 陈帅彬
 * @date 2023/5/10 16:24
 */
@Mapper
@SuppressWarnings({"all"})
public interface JournalMapper {

    List<Journal> selectjournal(@Param("username") String username);

    Journal selectlist(@Param("username") String username);

    // 数据修改
    int repair(@Param("username") String username,@Param("day") String day,@Param("week") String week,@Param("all") String all);

    Journal_time selecttime(@Param("username") String username);

    List<Journal> selecttimeall(@Param("starttime") String starttime,@Param("endtime") String endtime);

    Journal selectend(String username);

    Journal selectid(Integer id);

    int deleteid(Integer id);

    int addjournal(@Param("username") String username,@Param("content") String content, @Param("starttime") String starttime,@Param("endtime") String endtime,@Param("sumTime") Long sumTime);

    int updatejournal(@Param("username") String username,@Param("content") String content,@Param("endtime") String endtime,@Param("sumTime") Long sumTime);
}
