package com.example.demo.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiAccessConfig {
    @Bean
    public RateLimiter rateLimiter() {
        // 每秒允许1个请求
        return RateLimiter.create(1.0);
    }
}