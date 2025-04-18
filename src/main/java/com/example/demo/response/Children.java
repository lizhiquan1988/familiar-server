package com.example.demo.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@JsonDeserialize(using = ChildrenDeserializer.class) // 使用自定义反序列化器
public class Children {
    private Map<String, ChildCategory> items = new LinkedHashMap<>();
    private String container;

    // 空数组时的构造方法
    public Children() {}

    // 有数据时的构造方法
    public Children(Map<String, ChildCategory> items, String container) {
        this.items = items;
        this.container = container;
    }
}
