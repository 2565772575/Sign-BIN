package com.chen.Sign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.Sign.pojo.Blogsumdata;
import com.chen.Sign.pojo.Boilcollect;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author 陈帅彬
 * @date 2023/4/29 11:18
 */
@Repository
@SuppressWarnings({"all"})
@Mapper
public interface BoilcollectMapper extends BaseMapper<Boilcollect> {
}
