package com.chen.Sign.config;

import com.chen.Sign.Interceptor.MyInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SuppressWarnings({"all"})
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        WebMvcConfigurer.super.addInterceptors(registry);
        registry.addInterceptor(new MyInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login",
                        "/user/register",
                        "/user/forgetpassword",
                        "/getimages",
                        "/user/mail",
                        "/user/forgetmail",
                        "/user/registerwx",
                        "/images/**",
                        "/blog/image/**",
                        "/blog/addimage",
                        "/face/**");
    }
}





