package com.chen.Sign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.Sign.pojo.Blogsumdata;
import com.chen.Sign.pojo.Boil;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author 陈帅彬
 * @date 2023/4/29 11:17
 */
@Repository
@SuppressWarnings({"all"})
@Mapper
public interface BoilMapper extends BaseMapper<Boil> {

    public int addviews(@Param("views") Integer views, @Param("id") Integer id);
}
