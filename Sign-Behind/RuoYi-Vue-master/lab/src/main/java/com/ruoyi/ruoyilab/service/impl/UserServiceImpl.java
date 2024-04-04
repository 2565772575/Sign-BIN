package com.ruoyi.ruoyilab.service.impl;


import com.ruoyi.ruoyilab.mapper.UserMapper;
import com.ruoyi.ruoyilab.pojo.Blog;
import com.ruoyi.ruoyilab.pojo.User;
import com.ruoyi.ruoyilab.service.UserService;
import com.ruoyi.ruoyilab.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 陈帅彬
 * @date 2023/5/9 20:13
 */
@Service
@SuppressWarnings({"all"})
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;

    @Override
    public int resettingpwd(String password,String username) {
        // 重置密码
        int result = userMapper.setpwd(DigestUtils.md5Hex(password),username);
        return result;
    }

    // 删除user,journal,journal_time,blog,blogsumdata,collect,comment,face,liked,message,relation,schedule,user_like中所有关于该用户的信息
    @Override
    public int deleteuser(String username) {
        // 拿到该用户的头像文件
        User user = userMapper.selectname(username);
        String profile_photo = user.getProfile_photo();
        // 删除静态资源
        if (profile_photo != null) {
            // 删除原来头像的文件
            String[] split = profile_photo.split("/");
            String s = split[2];
            String filepath = "/usr/local/signt/images/" + s;
            File file1 = new File(filepath);
            if (file1.exists()) {
                file1.delete();
            }
        }
        // 拿到该用户博客的图片数据
        // 获取该用户所有博客的数据
        List<Blog> blogList = userMapper.selectblog(username);
        List<String> filenamelist = new ArrayList<>();
        List<Integer> blogidlist = new ArrayList<>();
        for (Blog blog : blogList) {
            blogidlist.add(blog.getId());
            String filename = blog.getFilename();
            if (filename != null) {
                filenamelist.add(filename);
            }
        }
        // 删除博客的图片数据
        for (String filename : filenamelist) {
            String[] split = filename.split("/");
            String s = split[2];
            String filepath = "/usr/local/signt/images/" + s;
            File file1 = new File(filepath);
            if (file1.exists()) {
                file1.delete();
            }
        }
        // user √
        int result = userMapper.deleteuser(username);
        // journal √
        int result1 = userMapper.deletejournal(username);
        // journal_time √
        int result2 = userMapper.deletejournal_time(username);
        // blog √
        int result3 = userMapper.deleteblog(username);
        // blogsumdata √
        int resul4 = userMapper.deleteblogsumdata(username);
        // face √
        int result7 = userMapper.deleteface(username);
        // schedule √
        int result11 = userMapper.deleteschedule(user.getId());
        // user_like √
        int result12 = userMapper.deleteuser_like(user.getId());
        return result;
    }

    @Override
    public List<UserVo> selectalluser() {
         List<UserVo> result = userMapper.selectalluser();
         return result;
    }
}
