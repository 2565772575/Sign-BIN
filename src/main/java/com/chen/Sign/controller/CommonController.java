package com.chen.Sign.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.chen.Sign.common.Result;
import com.chen.Sign.mapper.UserMapper;
import com.chen.Sign.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @author 陈帅彬
 * @date 2023/2/26 10:33
 */
@Slf4j
@RestController
@RequestMapping("/common")
@SuppressWarnings({"all"})
public class CommonController {

    @Value("${lab.path}")
    private String basePath;


    @Autowired
    private UserMapper userMapper;
    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result upload(@RequestPart("file") MultipartFile file) {
        log.info(file.toString());

        // 原始文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String filename = UUID.randomUUID().toString() + suffix;

        // 创建一个目录对象，判断是否存在，不存在则创建
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            file.transferTo(new File(basePath + filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Result(1,"文件上传成功",filename);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String filename, HttpServletResponse response) {
        try {
            // 输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + filename));

            // 输出流，通过输出流将文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            // 关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("/uploadfile")
    public Result uploadfile(@RequestPart("file") MultipartFile file, HttpServletRequest request) {
        long starttime = System.currentTimeMillis();
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        log.info(file.toString());

        // 原始文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String filename = UUID.randomUUID().toString() + suffix;

        // 创建一个目录对象，判断是否存在，不存在则创建
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            file.transferTo(new File(basePath + filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String address = "/images/" + filename;
        long endtime = System.currentTimeMillis();
        log.info("上传文件花费的时间为：" + (endtime - starttime) + "毫秒");
        return new Result(1,"文件上传成功",address);
    }
}
