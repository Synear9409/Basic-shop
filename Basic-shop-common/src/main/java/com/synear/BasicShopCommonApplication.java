package com.synear;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.synear.ThreadPool.dao")
//@ServletComponentScan(basePackages = "com.synear.ThreadPool.listener")
//@EnableAspectJAutoProxy
public class BasicShopCommonApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicShopCommonApplication.class);
       /* ConfigurableApplicationContext context = SpringApplication.run(BasicShopCommonApplication.class);
        TestService bean = context.getBean(TestService.class);
        System.out.println(bean.getClass());  //output： class com.synear.Aspect.TestServiceImp$$EnhancerBySpringCGLIB$$1344bf4d  CGLIB动态代理对象
*/
    }
}
