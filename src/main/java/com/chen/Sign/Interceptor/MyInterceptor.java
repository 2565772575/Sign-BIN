package com.chen.Sign.Interceptor;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.chen.Sign.utils.JWTUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SuppressWarnings({"all"})
public class MyInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Map<String, Object> map = new HashMap<>();
        // 获取请求头中令牌
        String token = request.getHeader("token");
        if (token != null) {
            log.info("当前token为: [{}]", token);
            try {
                JWTUtils.verify(token);// 验证令牌
                return true; // 放行请求
            } catch (SignatureVerificationException e) {
                e.printStackTrace();
                map.put("msg", "无效签名！");
            } catch (TokenExpiredException e) {
                e.printStackTrace();
                map.put("msg", "token过期");
            } catch (AlgorithmMismatchException e) {
                e.printStackTrace();
                map.put("msg", "token过期");
            } catch (Exception e) {
                e.printStackTrace();
                map.put("msg", "token无效！！！");
            }
            map.put("state", false); // 设置状态
            // 将map 专为json jackson
            Object o = JSON.toJSON(map);
            String json = new ObjectMapper().writeValueAsString(map);
            response.setContentType("application;charset=UTF-8");
            response.getWriter().println(o);
            return false;
        }
        String json = new ObjectMapper().writeValueAsString("token为空");
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println("token为空");
        return false;
    }



    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
