package com.chen.Sign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.Sign.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author 陈帅彬
 * @date 2023/3/13 19:57
 */
@Repository
@SuppressWarnings({"all"})
public interface CommentMapper extends BaseMapper<Comment> {

}
