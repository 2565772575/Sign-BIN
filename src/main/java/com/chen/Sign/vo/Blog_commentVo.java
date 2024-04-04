package com.chen.Sign.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"all"})
public class Blog_commentVo {

    Long id;

    Long articleId;

    Long rootId;

    String content;

    Long toCommentUserId;

    String toCommentUserName;

    Long toCommentId;

    Long createBy;

    LocalDateTime createTime;

    String username;

    List<Blog_commentVo> children;

}
