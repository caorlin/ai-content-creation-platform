package com.caoerlin.aicontentcreation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class AiContentCreationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiContentCreationApplication.class, args);
    }

}
