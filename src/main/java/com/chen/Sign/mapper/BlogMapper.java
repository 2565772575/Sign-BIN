package com.chen.Sign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chen.Sign.pojo.Blog;
import com.chen.Sign.pojo.User;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@SuppressWarnings({"all"})
@Mapper
public interface BlogMapper extends BaseMapper<Blog> {



    @MapKey("id")
    public Map<String, Object> selectByNameBlog(String nickname);

    public int addviews(@Param("views") Integer views, @Param("id") Integer id);


    public Page<Blog> selectPageVo(@Param("page") Page<Blog> page);

    public List<Blog> select00(@Param("search") String search,@Param("type") Integer type);
    public List<Blog> select01(@Param("search") String search,@Param("type") Integer type);
    public List<Blog> select02(@Param("search") String search,@Param("type") Integer type);
    public List<Blog> select03(@Param("search") String search,@Param("type") Integer type);
    public List<Blog> select10(@Param("search") String search,@Param("type") Integer type);
    public List<Blog> select11(@Param("search") String search,@Param("type") Integer type);
    public List<Blog> select12(@Param("search") String search,@Param("type") Integer type);
    public List<Blog> select13(@Param("search") String search,@Param("type") Integer type);
    public List<Blog> select20(@Param("search") String search,@Param("type") Integer type);
    public List<Blog> select21(@Param("search") String search,@Param("type") Integer type);
    public List<Blog> select22(@Param("search") String search,@Param("type") Integer type);
    public List<Blog> select23(@Param("search") String search,@Param("type") Integer type);
}
