package com.chen.Sign.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.Sign.mapper.CommentMapper;
import com.chen.Sign.pojo.Comment;
import com.chen.Sign.service.CommentService;
import org.springframework.stereotype.Service;

/**
 * @author 陈帅彬
 * @date 2023/3/13 20:00
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
}
