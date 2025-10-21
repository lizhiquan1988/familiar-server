package com.example.demo.service;

import com.example.demo.utils.ApiAppId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WeatherForecastService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherForecastService.class);

    private final String WEATHER_FORECAST_URL = "https://api.weatherapi.com/v1/forecast.json";

    private final String OPEN_WEATHER_BASE_URL = "https://api.openweathermap.org/data/3.0/onecall";

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

    public Map<String, Object> getOpenWeatherForecast() {
        RestTemplate restTemplate = new RestTemplate();
        // 构造请求 URL
        String url = UriComponentsBuilder.fromHttpUrl(OPEN_WEATHER_BASE_URL)
                .queryParam("lat", 35.663613)
                .queryParam("lon", 139.732293)
                .queryParam("exclude", "minutely,hourly")
                .queryParam("appid", ApiAppId.openWeatherApiKey)
                .queryParam("units", "metric") // 注意拼写 metric
                .build()
                .toUriString();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String body = response.getBody();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(body);

            // 提取各部分
            JsonNode current = root.path("current");
            JsonNode today = root.path("daily").get(0);
            JsonNode nextFiveDays = root.path("daily");

            // 转成 JSON 字符串方便单独传给 ChatGPT
            Map<String, Object> result = new HashMap<>();
            result.put("current", mapper.writeValueAsString(current));
            result.put("today", mapper.writeValueAsString(today));

            // 提取未来5天（索引 1~6）
            List<String> nextDaysList = new ArrayList<>();
            for (int i = 1; i <= 6 && i < nextFiveDays.size(); i++) {
                nextDaysList.add(mapper.writeValueAsString(nextFiveDays.get(i)));
            }
            result.put("future6days", nextDaysList);
            return result; // 返回 JSON 字符串
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Map.of("error", "调用 OpenWeatherMap API 失败: " + e.getMessage());
        }
    }
}