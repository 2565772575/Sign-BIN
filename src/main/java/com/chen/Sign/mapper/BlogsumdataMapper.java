package com.chen.Sign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.Sign.pojo.Blogsumdata;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author 陈帅彬
 * @date 2023/3/8 10:39
 */
@Repository
@SuppressWarnings({"all"})
@Mapper
public interface BlogsumdataMapper extends BaseMapper<Blogsumdata> {
}
