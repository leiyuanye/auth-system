package com.example.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@MapperScan(basePackages = "com.example.auth.mapper")
public class AuthApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(AuthApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
        System.out.println("===============================================");
        System.out.println("  权限管理系统(前后端分离版)启动成功!");
        System.out.println("  后端API: http://localhost:8080/");
        System.out.println("  前端(开发模式): http://localhost:5173/");
        System.out.println("  默认账号: admin / admin123");
        System.out.println("===============================================");
    }
}
