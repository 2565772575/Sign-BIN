package com.chen.Sign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.Sign.pojo.Blogimage;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author 陈帅彬
 * @date 2023/4/17 15:13
 */
@Repository
@SuppressWarnings({"all"})
@Mapper
public interface BlogimageMapper extends BaseMapper<Blogimage> {

}
