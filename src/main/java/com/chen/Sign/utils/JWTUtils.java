package com.chen.Sign.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Calendar;
import java.util.Map;

@SuppressWarnings({"all"})
public class JWTUtils {
    private static final String SING = "@$#4!HJ5ZN";

    @Autowired
    RedisTemplate redisTemplate;

    //1. 生成token  header.payload.sing
    public static String getToken(Map<String, String> map) {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE, 7/*Calendar.SECOND,10*/); // 默认7天过期
        // 创建jwt bulider
        JWTCreator.Builder builder = JWT.create();
        // payload
        map.forEach((k, v) -> {
            builder.withClaim(k, v);
        });
        // 指定令牌过期时间
        String token = builder.withExpiresAt(instance.getTime()).sign(Algorithm.HMAC256(SING));
        return token;
    }

    // 验证token
    public static DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC256(SING)).build().verify(token);
    }

    //3. 获取token信息方法
    public static DecodedJWT getTokenInfo(String token) {
        DecodedJWT verify = JWT.require(Algorithm.HMAC256(SING)).build().verify(token);
        return verify;
    }


}
