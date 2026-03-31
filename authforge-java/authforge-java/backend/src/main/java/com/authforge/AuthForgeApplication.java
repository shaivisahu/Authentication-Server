package com.authforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AuthForgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthForgeApplication.class, args);
    }
}
