package com.ruoyi.ruoyilab.service;


import com.ruoyi.ruoyilab.vo.UserVo;

import java.util.List;

/**
 * @author 陈帅彬
 * @date 2023/5/9 20:12
 */
public interface UserService {
    int resettingpwd(String password,String username);

    int deleteuser(String username);

    List<UserVo> selectalluser();

}
