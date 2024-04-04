package com.chen.Sign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.Sign.pojo.Journal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper
@SuppressWarnings({"all"})
public interface JournalMapper extends BaseMapper<Journal> {

    // 插入学习记录
    public int insertJournal(Journal journal);

    // 根据用户名返回所有数据
    public Map<String, Object> selectByName(String username);

    //查询一天内的时间数据
    public List<Journal> selectByNameDay(@Param("username") String username);

    //查询一周内的时间数据
    public List<Journal> selectByNameWeek(String username);

    // 查询所有的时间数据
    public List<Journal> selectByNameAll(String username);


}
