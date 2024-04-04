package com.chen.Sign.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.Sign.common.Result;
import com.chen.Sign.mapper.BlogMapper;
import com.chen.Sign.mapper.BlogsumdataMapper;
import com.chen.Sign.mapper.CollectMapper;
import com.chen.Sign.mapper.UserMapper;
import com.chen.Sign.pojo.Blog;
import com.chen.Sign.pojo.Blogsumdata;
import com.chen.Sign.pojo.Collect;
import com.chen.Sign.pojo.User;
import com.chen.Sign.service.BlogService;
import com.chen.Sign.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author 陈帅彬
 * @date 2023/2/28 19:12
 */
@RestController
@Slf4j
@RequestMapping("/collect")
@SuppressWarnings({"all"})
public class CollectController {

    @Autowired
    private CollectMapper collectMapper;

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private BlogsumdataMapper blogsumdataMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BlogService blogService;

    // 收藏博客
    @PostMapping("/{id}")
    public Result collect(HttpServletRequest request, @PathVariable("id") Integer id) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String username = tokenInfo.getClaim("username").asString();
        Blog blog = blogMapper.selectById(id);
        String username1 = blog.getUsername();
        LambdaQueryWrapper<Blogsumdata> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Blogsumdata::getUsername,username1);
        Blogsumdata blogsumdata = blogsumdataMapper.selectOne(queryWrapper1);
        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Collect::getBlog_id, id);
        queryWrapper.eq(Collect::getUsername, username);
        Collect collect1 = collectMapper.selectOne(queryWrapper);
        if (collect1 != null) {
            Integer id1 = collect1.getId();
            int i = collectMapper.deleteById(id1);
            blogsumdata.setSumcollect(blogsumdata.getSumcollect()-1);
            blogsumdataMapper.updateById(blogsumdata);
            return new Result(2, "取消收藏成功", "");
        }
        blogsumdata.setSumcollect(blogsumdata.getSumcollect()+1);
        blogsumdataMapper.updateById(blogsumdata);
        Collect collect = new Collect();
        collect.setUsername(username);
        collect.setBlog_id(id);
        int insert = collectMapper.insert(collect);
        if (insert > 0) {
            return new Result(1, "收藏成功", "");
        }
        return new Result(0, "收藏失败", "");
    }

    // 查看收藏博客--小程序
    @GetMapping("/wxfavorite")
    public Result wxfavorite(HttpServletRequest request) {
        int sum = 0;
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String username = tokenInfo.getClaim("username").asString();
        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Blog> queryWrapper1 = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<User> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper.eq(Collect::getUsername, username);
        List<Collect> collects = collectMapper.selectList(queryWrapper);
        List<Blog> list = new ArrayList<>();
        for (Collect collect : collects) {
            Integer blog_id = collect.getBlog_id();
            queryWrapper1.eq(Blog::getId, blog_id);
            Blog blog = blogMapper.selectOne(queryWrapper1);
            list.add(blog);
            sum++;
        }
        queryWrapper2.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper2);
        HashMap<Object, Object> map = new HashMap<>();
        map.put("blogdata", list);
        map.put("collectsum", sum);
        map.put("username", user.getUsername());
        map.put("profile_photo", user.getProfile_photo());
        map.put("nickname", user.getNickname());
        return new Result(1, "收藏博客数据返回成功", map);
    }

    // 查看收藏博客--pc端
    @GetMapping("/favorite")
    public Result favorite(Integer page, Integer pageSize,HttpServletRequest request) {
        int sum = 0;
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String username = tokenInfo.getClaim("username").asString();
        Page<Object> pageInfo = new Page<>();
        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Blog> queryWrapper1 = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<User> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper.eq(Collect::getUsername, username);
        List<Collect> collects = collectMapper.selectList(queryWrapper);
        List<Blog> list = new ArrayList<>();
        for (Collect collect : collects) {
            Integer blog_id = collect.getBlog_id();
            queryWrapper1.eq(Blog::getId, blog_id);
            Blog blog = blogMapper.selectOne(queryWrapper1);
            list.add(blog);
            sum++;
        }
        queryWrapper2.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper2);
        Page pages = getPages(page, pageSize, list);
        HashMap<Object, Object> map = new HashMap<>();
        map.put("blogdata", pages);
        map.put("collectsum", sum);
        map.put("username", user.getUsername());
        map.put("profile_photo", user.getProfile_photo());
        map.put("nickname", user.getNickname());
        return new Result(1, "收藏博客数据返回成功", map);
    }

    /**
     * 分页函数
     * @author pochettino
     * @param currentPage   当前页数
     * @param pageSize  每一页的数据条数
     * @param list  要进行分页的数据列表
     * @return  当前页要展示的数据
     */
    private Page getPages(Integer currentPage, Integer pageSize, List list) {
        Page page = new Page();
        if(list==null){
            return  null;
        }
        int size = list.size();

        if(pageSize > size) {
            pageSize = size;
        }
        if (pageSize!=0){
            // 求出最大页数，防止currentPage越界
            int maxPage = size % pageSize == 0 ? size / pageSize : size / pageSize + 1;

            if(currentPage > maxPage) {
                currentPage = maxPage;
            }
        }
        // 当前页第一条数据的下标
        int curIdx = currentPage > 1 ? (currentPage - 1) * pageSize : 0;

        List pageList = new ArrayList();

        // 将当前页的数据放进pageList
        for(int i = 0; i < pageSize && curIdx + i < size; i++) {
            pageList.add(list.get(curIdx + i));
        }

        page.setCurrent(currentPage).setSize(pageSize).setTotal(list.size()).setRecords(pageList);
        return page;
    }



}
