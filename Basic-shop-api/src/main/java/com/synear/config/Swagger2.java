package com.synear.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger2 {

    // http:localhost:9409/doc.html   使用bootstrap美化后访问地址

    // 配置swagger2核心配置 docket
    @Bean
    public Docket createRestApi(){
        return new Docket(DocumentationType.SWAGGER_2)     // 指定api类型为swagger2
                .apiInfo(apiInfo())                       // 用于定义api文档汇总信息
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.synear.controller"))   // 指定controller包
                .paths(PathSelectors.any())               // 所有controller路径
                .build();
    }

    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title("天天吃货 基础电商平台接口api")      // 文档页标题
                .contact(new Contact("Synear",     // 作者信息
                        "https://www.baidu.com",
                        "synear_chen@qq.com"))
                .description("专为天天吃货提供的api文档")  // 简介
                .version("1.0.1")   // 文档版本号
                .termsOfServiceUrl("https://www.baidu.com") // 服务地址
                .build();
    }
}
