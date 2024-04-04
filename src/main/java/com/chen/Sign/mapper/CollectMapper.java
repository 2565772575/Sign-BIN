package com.chen.Sign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.Sign.pojo.Collect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author 陈帅彬
 * @date 2023/2/28 19:08
 */
@Repository
@SuppressWarnings({"all"})
@Mapper
public interface CollectMapper extends BaseMapper<Collect> {

    Collect selectcollectstate(@Param("username") String username, @Param("id") Integer id);
}
