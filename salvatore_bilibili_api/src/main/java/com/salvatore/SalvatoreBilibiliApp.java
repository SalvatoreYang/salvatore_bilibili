package com.salvatore;


import com.salvatore.bilibili.service.config.WebSocketConfig;
import com.salvatore.bilibili.service.websocket.WebSocketService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
public class SalvatoreBilibiliApp {
    public static void main(String[] args) {
        ApplicationContext app = SpringApplication.run(SalvatoreBilibiliApp.class, args);
        WebSocketService.setApplicationContext(app);
    }
}
