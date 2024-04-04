package com.ruoyi.ruoyilab.controller;

import com.ruoyi.ruoyilab.common.R;
import com.ruoyi.ruoyilab.service.UserService;
import com.ruoyi.ruoyilab.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 陈帅彬
 * @date 2023/5/9 20:10
 */
@Slf4j
@RestController
@SuppressWarnings({"all"})
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

     // 用户重置密码
    @PutMapping()
    public R resetting(String password,String username) {
        int result = userService.resettingpwd(password,username);
        if (result > 0) {
            return R.success("重置密码成功","");
        } else {
            return R.error("重置密码失败");
        }
    }


    
    // 注销用户
    @DeleteMapping()
    public R delete(String username) {
        int result = userService.deleteuser(username);
        if (result > 0) {
            return R.success("注销用户成功","");
        } else {
            return R.error("注销用户失败");
        }
    }

    // 查询所有用户信息
    @GetMapping()
    public R all() {
         List<UserVo> result = userService.selectalluser();
         return R.success("查询所有信息成功",result);
    }

}
