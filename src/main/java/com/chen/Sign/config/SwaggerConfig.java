package com.chen.Sign.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

import static springfox.documentation.service.ApiInfo.DEFAULT_CONTACT;

@Configuration  //开启swagger2
@EnableSwagger2
@SuppressWarnings({"all"})
public class SwaggerConfig {

 /*   @Bean
    public Docket docket1() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("A");
    }

    @Bean
    public Docket docket2() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("B");
    }

    @Bean
    public Docket docket3() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("C");
    }*/

    // 配置了Swagger的Docket的Bean实例
    @Bean
    public Docket docket(Environment environment) {

        // 设置要显示的Swagger环境
        // Profiles profiles = Profiles.of("dev", "test");
        // 通过environment.acceptsProfiles判断是否处在自己设定的环境当中
        // boolean flag = environment.acceptsProfiles(profiles);


        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                // enable是否启动Swagger,如果为False,则Swagger不能再浏览器中访问
                // .enable(false)
                .select()
                // RequestHandlerSelectors.basePackage 配置要扫描接口的方式
                // basePackage: 要指定要扫描的包
                // any():  扫描全部
                // none(): 不扫描
                // withClassAnnotation: 扫描类上的注解
                // withMethodAnnotation: 扫描方法上的注解
                .apis(RequestHandlerSelectors.basePackage("com.chen.Sign"))

                // paths() 过滤什么路径
                // .paths(PathSelectors.ant("/chen/**"))
                .build();
    }

    // 配置Swagger 信息 = apiInfo
    private ApiInfo apiInfo() {

        //作者信息
        Contact contact = new Contact("陈帅彬", "https://swagger.io/", "2565772575@qq.com");

        return new ApiInfo(
                "实验室考勤网站接口文档",
                "开卷",
                "1.0",
                "https://swagger.io/",
                contact,
                "Apache 2.0",
                "http://www.apache.org/licenses/LICENSE-2.0",
                new ArrayList());
    }

}
