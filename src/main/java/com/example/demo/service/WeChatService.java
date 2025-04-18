package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WeChatService {

    @Value("${wechat.appId}")
    private String appId;

    @Value("${wechat.appSecret}")
    private String appSecret;

    private static final Logger logger = LoggerFactory.getLogger(WeChatService.class);

    private final String WECHAT_URL = "https://api.weixin.qq.com/sns/jscode2session";

    // 使用 RestTemplate 发送请求并获取响应
    public String getOpenid(String code) {
        String url = UriComponentsBuilder.fromHttpUrl(WECHAT_URL)
                .queryParam("appid", appId)
                .queryParam("secret", appSecret)
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .toUriString();

        RestTemplate restTemplate = new RestTemplate();
        try {
            // 发送 GET 请求
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            // 检查响应状态码
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody(); // 返回微信 API 返回的 JSON 字符串
            } else {
                // 记录错误日志
                logger.error("微信 API 请求失败，状态码: {}", response.getStatusCodeValue());
                return "{\"errcode\": 500, \"errmsg\": \"微信 API 请求失败\"}";
            }
        } catch (Exception e) {
            // 捕获异常并记录日志
            logger.error("微信 API 请求异常: {}", e.getMessage(), e);
            return "{\"errcode\": 500, \"errmsg\": \"微信 API 请求异常: " + e.getMessage() + "\"}";
        }
    }
}

