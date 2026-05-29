package com.caoerlin.aicontentcreation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan(basePackages = "com.caoerlin.aicontentcreation.mapper")
public class AiContentCreationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiContentCreationApplication.class, args);
    }

}
