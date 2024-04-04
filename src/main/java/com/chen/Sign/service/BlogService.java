package com.chen.Sign.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.Sign.common.Result;
import com.chen.Sign.pojo.Blog;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public interface BlogService extends IService<Blog> {




    public Page<Blog> search(int page, int pageSize,String search);

    void deleteById(Integer id) throws IOException;

    void insertById(Integer id) throws IOException;
}

