package com.xima.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Xima聊天应用程序入口类
 */
@SpringBootApplication
@EnableScheduling
public class XimaApplication {

    public static void main(String[] args) {
        SpringApplication.run(XimaApplication.class, args);
    }
} 