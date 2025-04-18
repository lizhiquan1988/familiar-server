package com.example.demo.controller;

import com.alibaba.fastjson2.JSONObject;
import com.example.demo.service.WeChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class WeChatController {

    private static final Logger logger = LoggerFactory.getLogger(WeChatController.class);

    @Autowired
    private WeChatService weChatService;

    @GetMapping("/mini/api/get-openid")
    public String getOpenid(HttpServletRequest request) {
        // 传入前端的 code 请求微信 API
        String code = request.getParameter("code");
        String code1 = request.getQueryString();
        logger.info("Received request: method={}, URl={}, code={}, code1={}", request.getMethod(), request.getRequestURL(), code, code1);
        String response = weChatService.getOpenid(code);
        // 解析微信返回的 JSON 数据
        JSONObject jsonResponse = JSONObject.parseObject(response);

        // 从返回的 JSON 中获取 openid
        String openid = (String) jsonResponse.get("openid");

        // 如果 openid 存在，返回 openid；否则返回错误信息
        if (openid != null) {
            return "{\"openid\":\"" + openid + "\"}";
        } else {
            return "{\"error\":\"Failed to get openid\"}";
        }
    }
}

