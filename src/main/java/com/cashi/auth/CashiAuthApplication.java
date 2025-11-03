package com.cashi.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CashiAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(CashiAuthApplication.class, args);
    }
}
