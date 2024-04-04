package com.chen.Sign.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.Sign.pojo.Blog;
import com.chen.Sign.mapper.BlogMapper;
import com.chen.Sign.service.BlogService;
import org.apache.ibatis.annotations.Param;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@SuppressWarnings({"all"})
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Autowired
    BlogMapper blogMapper;

    @Autowired
    BlogService blogService;

    private RestHighLevelClient client;

    @Override
    public Page<Blog> search(int page, int pageSize,String search) {
        Page<Blog> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(search != null, Blog::getTitle, search);
        queryWrapper.orderByDesc(Blog::getCreate_time);
        blogService.page(pageInfo,queryWrapper);
        return pageInfo;
    }

    // 同步删除ES对应文档
    @Override
    public void deleteById(Integer id) throws IOException {
        DeleteRequest request = new DeleteRequest("blog", id.toString());
        client.delete(request, RequestOptions.DEFAULT);
    }

    // 同步增加修改ES对应文档
    @Override
    public void insertById(Integer id) throws IOException {
        Blog blog = getById(id);
        IndexRequest request = new IndexRequest("blog").id(id.toString());
        request.source(JSON.toJSONString(blog), XContentType.JSON);
        client.index(request,RequestOptions.DEFAULT);
    }

    public boolean updateviews(@Param("views") Integer views, @Param("id") Integer id) {
        int addviews = blogMapper.addviews(views, id);
        if (addviews > 0) {
            return true;
        } else {
            return false;
        }
    }
}
