package com.chen.Sign.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baidu.aip.face.AipFace;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chen.Sign.common.Result;
import com.chen.Sign.mapper.FaceMapper;
import com.chen.Sign.mapper.UserMapper;
import com.chen.Sign.pojo.Face;
import com.chen.Sign.pojo.User;
import com.chen.Sign.service.FaceService;
import com.chen.Sign.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Base64;

/**
 * @author 陈帅彬
 * @date 2023/3/22 20:30
 */
@Slf4j
@RestController
@RequestMapping("/face")
@SuppressWarnings({"all"})
public class FaceController {

    @Autowired
    private AipFace aipFace;

    @Autowired
    private FaceService faceService;

    @Autowired
    private FaceMapper faceMapper;

    @Autowired
    private UserMapper userMapper;

    @Value("${lab.path}")
    private String basePath;

    final Base64.Decoder decoder = Base64.getDecoder();
    final Base64.Encoder encoder = Base64.getEncoder();

    /**
     * 注册人脸
     *
     * @param userName
     * @param faceBase
     * @return
     * @throws IOException
     */
    @PostMapping("/register")
    public Result register(HttpServletRequest request, String faceBase) throws IOException {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        if (faceBase != null) {
            // 文件上传的地址
            String filePath = basePath;
            // 图片名称
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            String fileName = basePath + uuid + username + ".png";
            // 往数据库里插入一条用户数据
            Face face = new Face();
            face.setUsername(username);
            face.setUserPhoto(fileName);
            int insert = faceMapper.insert(face);
            // 保存上传摄像头捕获的图片
            test(faceBase, fileName);
            // 向百度云人脸库插入一张人脸
            faceSetAddUser(aipFace, faceBase, username);
            return new Result(1, "注册成功", "");
        } else {
            return new Result(1, "人脸数据为空，注册失败", "");
        }
    }

    /**
     * 登录人脸
     *
     * @param faceBase
     * @return
     */
    @PostMapping("/login")
    public Result login(String faceBase) throws UnsupportedEncodingException {
        // 进行人像数据对比
        HashMap<String, Object> result = verifyUser(faceBase, aipFace);
        Double score = (Double) result.get("score");
        if (score > 80) {
            String username = (String) result.get("username");
            User login = userMapper.selectUserByName(username);
            int id = login.getId();
            HashMap<String, String> payload = new HashMap<>();
            payload.put("id", String.valueOf(login.getId()));
            System.out.println(String.valueOf(login.getId()));
            payload.put("username", login.getUsername());
            // 生成JWT的令牌
            String token = JWTUtils.getToken(payload);
            System.out.println(token);
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            return new Result(1, "登录成功", map);
        } else {
            return new Result(1, "登录失败", "");
        }
    }

    @PutMapping("/update")
    public Result update(String faceBase, HttpServletRequest request) throws IOException {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        if (faceBase != null) {
            // 文件上传的地址
            String filePath = basePath;
            // 图片名称
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            String fileName = basePath + uuid + username + ".png";
            // 修改数据库里用户数据
            LambdaQueryWrapper<Face> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Face::getUsername, username);
            Face face = faceMapper.selectOne(queryWrapper);
            String userPhoto = face.getUserPhoto();
            face.setUserPhoto(fileName);
            int update = faceMapper.updateById(face);
            // 删除之前保存的图片
            update(fileName);
            // 保存上传摄像头捕获的图片
            test(faceBase, fileName);
            // 百度云人脸库修改人脸数据
            faceUpdateAddUser(aipFace, faceBase, username);
            return new Result(1, "修改成功", "");
        } else {
            return new Result(1, "人脸数据为空，修改失败", "");
        }
    }


    /**
     * 人脸比对
     *
     * @param imgBash64 照片转bash64格式
     * @return
     */
    public HashMap<String, Object> verifyUser(String imgBash64, AipFace client) {
        HashMap<String, Object> map = new HashMap<>();
        HashMap<String, Object> options = new HashMap<String, Object>();
        JSONObject res = client.search(imgBash64, "BASE64", "user_01", options);
        JSONObject user = (JSONObject) res.getJSONObject("result").getJSONArray("user_list").get(0);
        System.out.println("人脸比对结果：" + user.toString());
        Double score = (Double) user.get("score");
        Object username = user.get("user_id");
        map.put("score", score);
        map.put("username", username);
        return map;
    }

    /**
     * @param @param client 设定文件
     * @return 返回类型：void
     * @throws
     * @Title: facesetAddUser
     * @Description: 该方法的主要作用：人脸注册,给人脸库中注册一个人脸
     */
    public boolean faceSetAddUser(AipFace client, String faceBase, String username) {
        // 参数为数据库中注册的人脸
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("user_info", "user's info");
        JSONObject res = client.addUser(faceBase, "BASE64", "user_01", username, options);
        System.out.println("注册的人脸识别的数据：" + res.toString(2));
        return true;
    }

    /**
     * @param client
     * @param faceBase
     * @param username
     * @return
     */
    public boolean faceUpdateAddUser(AipFace client, String faceBase, String username) {
        // 参数为数据库中注册的人脸
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("user_info", "user's info");
        JSONObject res = client.updateUser(faceBase, "BASE64", "user_01", username, options);
        System.out.println("修改的人脸识别的数据：" + res.toString(2));
        return true;
    }

    /**
     * 测试base64转图片，并存储到本地
     *
     * @param base64 base64字符串
     */
    public Boolean test(String base64, String fileName) throws IOException {
        String tempFileName = fileName;
        String imgFilePath = tempFileName;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            //Base64解码
            byte[] b = decoder.decodeBuffer(base64);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    //调整异常数据
                    b[i] += 256;
                }
            }
            OutputStream out = new FileOutputStream(imgFilePath);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新人脸数据的时候删除原来的图片
     * @param path
     * @return
     */
    public Boolean update(String path) {
        File file = new File(path);
        boolean delete = file.delete();
        try {
            if (delete) {
                return true;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



}
