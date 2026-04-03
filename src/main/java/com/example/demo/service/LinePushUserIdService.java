package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LinePushUserIdService {

    private volatile String userId;

    public LinePushUserIdService(@Value("${line.push.user-id:}") String initialUserId) {
        this.userId = initialUserId;
    }

    public String getUserId() {
        return userId;
    }

    public void updateUserId(String userId) {
        if (StringUtils.hasText(userId)) {
            this.userId = userId;
        }
    }
}
