package com.chen.Sign.vo;

import com.chen.Sign.pojo.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 陈帅彬
 * @date 2023/3/15 21:08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"all"})
public class CommentVo {

    private Comment comment;

    List<CommentVo> children;


}
