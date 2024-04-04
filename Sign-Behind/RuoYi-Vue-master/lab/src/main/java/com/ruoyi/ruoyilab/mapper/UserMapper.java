package com.ruoyi.ruoyilab.mapper;


import com.ruoyi.ruoyilab.pojo.Blog;
import com.ruoyi.ruoyilab.pojo.User;
import com.ruoyi.ruoyilab.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 陈帅彬
 * @date 2023/5/9 20:12
 */
@Mapper
@SuppressWarnings({"all"})
public interface UserMapper  {

    int setpwd(@Param("password") String password,@Param("username") String username);

    int deleteuser(@Param("username") String username);

    List<UserVo> selectalluser();

    List<User> selectallusername();

    int updatestate(String username);

    User selectname(String username);

    List<User> selectnjname(@Param("starttime") String starttime,@Param("endtime") String endtime);


    int deletejournal(@Param("username") String username);

    int deletejournal_time(@Param("username") String username);

    int deleteblog(@Param("username") String username);

    int deleteblogsumdata(@Param("username") String username);

    int deleteface(@Param("username") String username);

    int deleteschedule(@Param("id") Integer id);

    int deleteuser_like(@Param("id") Integer id);

    List<Blog> selectblog(@Param("username") String username);
}
