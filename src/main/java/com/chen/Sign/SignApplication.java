package com.chen.Sign;

import com.chen.Sign.controller.MessageController;
import org.apache.catalina.filters.CorsFilter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@SpringBootApplication
@EnableScheduling
public class SignApplication {

    public static void main(String[] args) {
        SpringApplication.run(SignApplication.class, args);


    }
}
