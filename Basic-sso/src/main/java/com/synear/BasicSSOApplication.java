package com.synear;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
// 扫描 mybatis 通用 mapper 所在的包
@MapperScan(basePackages = "com.synear.mapper")
// 扫描所有包以及相关组件包
@ComponentScan(basePackages = {"com.synear", "idworker"})
public class BasicSSOApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicSSOApplication.class);
    }
}
