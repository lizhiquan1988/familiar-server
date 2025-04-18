package com.example.demo.controller;

import com.example.demo.service.WeatherForecastService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class WeatherForecastController {

    private static final Logger logger = LoggerFactory.getLogger(WeatherForecastController.class);

    @Autowired
    private WeatherForecastService weatherForecastService;

    @GetMapping("/mini/api/get-weather-info")
    public String getOpenid(HttpServletRequest request) {
        // 传入前端的 code 请求微信 API
        String latLng = request.getParameter("latLng");
        String code1 = request.getQueryString();
        logger.info("Received request: method={}, URl={}, code={}, code1={}", request.getMethod(), request.getRequestURL(), latLng, code1);
        return weatherForecastService.getWeatherInfo(latLng);
    }
}