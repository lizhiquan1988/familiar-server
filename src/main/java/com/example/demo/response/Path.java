package com.example.demo.response;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class Path {
    // getters
    // 动态键名处理（如 "0", "1"）
    private Map<String, PathItem> pathItems = new LinkedHashMap<>();

    // 单独处理 _container 字段
    @JsonProperty("_container")
    private String container;

    // 使用 @JsonAnySetter 处理动态键名
    @JsonAnySetter
    public void setPathItem(String key, Object value) {
        if (key.equals("_container")) {
            this.container = (String) value;
        }  else {
            // 转换逻辑
            ObjectMapper mapper = new ObjectMapper();
            PathItem child = mapper.convertValue(value, PathItem.class);
            this.pathItems.put(key, child);
        }
    }

}

