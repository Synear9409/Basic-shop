package com.synear.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域问题
 *  A 服务端解决方式
 *      1、 @CrossOrigin 注解  在Controller类加上，代表该类下所有方法支持跨域请求
 *                             在某个方法上加，表示该方法跨域请求
 *      2、实现WebMvcConfigurer接口，重写addCorsMappings方法（https://www.cnblogs.com/fanshuyao/p/14030944.html）
 *      3、CorsConfig过滤器
 *
 *  B 使用nginx反向代理也可
 */
@Configuration
public class CorsConfig {

    public CorsConfig() {
    }

    // 配置过滤器
    @Bean
    public CorsFilter corsFilter(){
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:8080");   // 设置允许跨域的地址

        // 设置是否发送cookie信息
        config.setAllowCredentials(true);

        // 设置允许请求的方式
        config.addAllowedMethod("*");

        // 设置允许的header
        config.addAllowedHeader("*");

        // 为url添加映射路径
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**",config);

        //返回重新定义好的corsSource
        return new CorsFilter(configurationSource);

    }
}
