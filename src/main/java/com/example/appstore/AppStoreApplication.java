package com.example.appstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@SpringBootApplication
public class AppStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppStoreApplication.class, args);
    }
    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }
}