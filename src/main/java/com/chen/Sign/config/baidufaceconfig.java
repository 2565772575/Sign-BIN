package com.chen.Sign.config;

import com.baidu.aip.face.AipFace;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 陈帅彬
 * @date 2023/3/22 20:31
 */
@Configuration
@SuppressWarnings({"all"})
public class baidufaceconfig {
    @Value("${face.baidu.appid}")
    private String appId;
    @Value(("${face.baidu.key}"))
    private String key;
    @Value(("${face.baidu.secret}"))
    private  String secret;
    @Bean
    public AipFace AipFace(){
        //System.out.println("人脸配置："+appId+"=="+key+"=="+secret);
        return new AipFace(appId,key,secret);
    }

}
