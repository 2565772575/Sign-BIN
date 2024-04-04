package com.chen.Sign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.Sign.pojo.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings({"all"})
@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {
}
