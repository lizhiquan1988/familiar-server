package com.example.demo.service;

import com.example.demo.utils.ApiAppId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WeatherForecastService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherForecastService.class);

    private final String WEATHER_FORECAST_URL = "https://api.weatherapi.com/v1/forecast.json";

    // 使用 RestTemplate 发送请求并获取响应
    public String getWeatherInfo(String latLng) {
        String url = UriComponentsBuilder.fromHttpUrl(WEATHER_FORECAST_URL)
                .queryParam("key", ApiAppId.weatherApiKey)
                .queryParam("q", latLng)
                .queryParam("aqi", "yes")
                .queryParam("days", "5")
                .queryParam("alerts", "yes")
                .queryParam("lang", "zh")
                .toUriString();

        RestTemplate restTemplate = new RestTemplate();
        try {
            // 发送 GET 请求
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            // 检查响应状态码
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody(); // 返回天气 API 返回的 JSON 字符串
            } else {
                // 记录错误日志
                logger.error("天气 API 请求失败，状态码: {}", response.getStatusCodeValue());
                return "{\"errcode\": 500, \"errmsg\": \"微信 API 请求失败\"}";
            }
        } catch (Exception e) {
            // 捕获异常并记录日志
            logger.error("天气 API 请求异常: {}", e.getMessage(), e);
            return "{\"errcode\": 500, \"errmsg\": \"微信 API 请求异常: " + e.getMessage() + "\"}";
        }
    }
}