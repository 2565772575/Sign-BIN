package com.chen.Sign.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chen.Sign.common.Result;
import com.chen.Sign.pojo.*;
import com.chen.Sign.mapper.CommentMapper;
import com.chen.Sign.mapper.LikedMapper;
import com.chen.Sign.mapper.UserMapper;
import com.chen.Sign.utils.JWTUtils;
import com.chen.Sign.vo.CommentVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 陈帅彬
 * @date 2023/3/10 20:22
 */
@RestController
@Slf4j
@SuppressWarnings({"all"})
@RequestMapping("/comment")
@CrossOrigin
public class CommentController {

    @Autowired
    private LikedMapper likedMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;


    // 评论/回复
    @PostMapping("/{id}")
    public Result add(HttpServletRequest request, @PathVariable("id") Integer id) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String uid = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        String content = request.getParameter("content");
        String parentid = request.getParameter("parentid");
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);
        Comment comment = new Comment();
        comment.setBlogid(id);
        comment.setUsername(username);
        comment.setNickname(user.getNickname());
        comment.setProfile_photo(user.getProfile_photo());
        comment.setContent(content);
        comment.setLikenum(0);
        comment.setParentid(parentid);
        int insert = commentMapper.insert(comment);
        return new Result(1, "评论成功", "");
    }

    // 删除评论
    @DeleteMapping("/{id}")
    public Result delete(HttpServletRequest request, @PathVariable("id") Integer id) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String uid = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getId,id);
        Comment comment = commentMapper.selectOne(queryWrapper);
        commentMapper.deleteById(comment.getId());
        LambdaQueryWrapper<Comment> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Comment::getParentid,id);
        List<Comment> comments = commentMapper.selectList(queryWrapper1);
        List<Integer> searchparentid = deletesearchparentid1(comments);
        deletesearchparentid2(searchparentid);
        return new Result(1, "删除评论成功", "");
    }

    // 返回评论数据
    @GetMapping("/{id}")
    public Result see(HttpServletRequest request, @PathVariable("id") Integer id) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String username = tokenInfo.getClaim("username").asString();
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getParentid,0).eq(Comment::getBlogid,id);
        List<Comment> comments = commentMapper.selectList(queryWrapper);
        List<CommentVo> result1 = new ArrayList<>();
        for (Comment comment : comments) {
            CommentVo commentVo = new CommentVo();
            commentVo.setComment(comment);
            commentVo.setChildren(null);
            result1.add(commentVo);
        }
        List<CommentVo> result = selectparentidlist1(result1);
        return new Result(1, "返回评论数据成功", result);
    }

    // 点赞评论
    @GetMapping ("like/{id}")
    public Result like(HttpServletRequest request, @PathVariable("id") Integer id) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String uid = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        LambdaQueryWrapper<Liked> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Liked::getCommentid,id).eq(Liked::getLikeid,uid);
        Liked liked = likedMapper.selectOne(queryWrapper);
        if (liked == null) {
            Liked liked1 = new Liked();
            liked1.setCommentid(id);
            liked1.setLikeid(uid);
            liked1.setStatus(1);
            int insert = likedMapper.insert(liked1);
            return new Result(1,"点赞成功","");
        }
        Integer status = liked.getStatus();
        if (status == 0) {
            liked.setStatus(1);
            int i = likedMapper.updateById(liked);
            return new Result(1,"点赞成功","");
        }
        liked.setStatus(0);
        int i = likedMapper.updateById(liked);
        return new Result(1,"取消点赞成功","");
    }

    // 删除评论并返回循环后的节点id
    public List<Integer> deletesearchparentid1(List<Comment> comments) {
        List<Integer> ids = new ArrayList<>();
        for (Comment comment : comments) {
            Integer id = comment.getId();
            ids.add(id);
            commentMapper.deleteById(id);
        }
        return ids;
    }

    // 根据接点递归删除
    public void deletesearchparentid2(List<Integer> ids) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        Map<Integer, Comment> map = new HashMap<>();
        List<Comment> result = new ArrayList<>();
        for (Integer id : ids) {
            queryWrapper.eq(Comment::getParentid,id);
            List<Comment> comments = commentMapper.selectList(queryWrapper);
            if (comments != null) {
                List<Integer> deletesearchparentid = deletesearchparentid1(comments);
                deletesearchparentid2(deletesearchparentid);
            }
        }
    }

    // 查找评论的id
    public List<CommentVo> selectparentidlist1(List<CommentVo> comments) {
        List<CommentVo> result = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        for (CommentVo commentVo : comments) {
            Integer id = commentVo.getComment().getId();
            List<CommentVo> commentVos = selectparentidlist2(id);
            commentVo.setChildren(commentVos);
            selectparentidlist1(commentVos);
            result.add(commentVo);
        }
        return result;
    }

    // 根据id将评论以树状结构返回
    public List<CommentVo> selectparentidlist2(Integer id) {
        List<CommentVo> result = new ArrayList<>();
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        List<Object> lists = new ArrayList<>();
        queryWrapper.eq(Comment::getParentid,id);
        List<Comment> comments = commentMapper.selectList(queryWrapper);
        for (Comment comment : comments) {
            CommentVo commentVo = new CommentVo();
            commentVo.setComment(comment);
            commentVo.setChildren(null);
            result.add(commentVo);
        }
        return result;
    }
}
