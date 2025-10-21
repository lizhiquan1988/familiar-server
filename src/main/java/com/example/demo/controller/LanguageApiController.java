package com.example.demo.controller;

import com.example.demo.LanguageLearningModel.LanguageLearningUserInfo;
import com.example.demo.LanguageLearningService.LanguageLearningUserInfoService;
import com.example.demo.service.ApiAccessService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/mini/language/api")
public class LanguageApiController {

    private static final Logger logger = LoggerFactory.getLogger(LanguageApiController.class);

    private LanguageLearningUserInfoService languageLearningUserInfoService;

    private LanguageLearningUserInfo languageLearningUserInfo;

    @Autowired
    private ApiAccessService apiAccessService;

    @GetMapping("/checkUserExist")
    public String miniProgramUserLogin(HttpServletRequest request) {
        String queryStr = getRequestJsonData(request);
        if (!queryStr.isEmpty()) {
            try {
                LanguageLearningUserInfo languageLearningUserInfo = languageLearningUserInfoService.getLanguageLearningUserInfo (queryStr);
                if (languageLearningUserInfo != null) {
                    // 提取需要的字段
                    Map<String, Object> jsonMap = new HashMap<>();
                    jsonMap.put("_id", languageLearningUserInfo.get_id());
                    jsonMap.put("isAdmin", languageLearningUserInfo.getIsAdmin());
                    jsonMap.put("registerDate", languageLearningUserInfo.getRegisterDate());
                    jsonMap.put("nickName", languageLearningUserInfo.getNickName());
                    jsonMap.put("avatarUrl", languageLearningUserInfo.getAvatarUrl());
                    jsonMap.put("openid", languageLearningUserInfo.getOpenId());
                    jsonMap.put("createTime", languageLearningUserInfo.getCreateTime());
                    jsonMap.put("updateTime", languageLearningUserInfo.getUpdateTime());
                    jsonMap.put("lastStudyDate", languageLearningUserInfo.getLastStudyDate());
                    jsonMap.put("studentGrade", languageLearningUserInfo.getStudentGrade());
                    jsonMap.put("studyDays", languageLearningUserInfo.getStudyDays());
                    jsonMap.put("studentName", languageLearningUserInfo.getStudentName());
                    jsonMap.put("userLevelDesc", languageLearningUserInfo.getUserLevelDesc());

                    // 将 Map 转换为 JSON 字符串
                    ObjectMapper objectMapper = new ObjectMapper();
                    return objectMapper.writeValueAsString(jsonMap);
                } else {
                    return "{\"error\": \"Failed to get user data.\"}";
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } else {
            return "{\"error\": \"Failed to get user data.\"}";
        }

        return "{\"error\": \"Failed to get user data.\"}";
    }

    private String getRequestJsonData(HttpServletRequest request) {
        ServletInputStream is = null;
        try {
            is = request.getInputStream();
            StringBuilder sb = new StringBuilder();
            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = is.read(buf)) != -1) {
                sb.append(new String(buf, 0, len));
            }
            System.out.println(sb.toString());
            return sb.toString();
        } catch (IOException e) {
            logger.error(e.getMessage());
            return "";
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

    }
}