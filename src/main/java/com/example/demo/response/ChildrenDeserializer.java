package com.example.demo.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;

public class ChildrenDeserializer extends JsonDeserializer<Children> {
    @Override
    public Children deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {

        JsonNode node = p.getCodec().readTree(p);
        Children children = new Children();

        // 处理空数组情况
        if (node.isArray() && node.isEmpty()) {
            return children;
        }

        // 处理对象情况
        if (node.isObject()) {
            ObjectMapper mapper = (ObjectMapper) p.getCodec();

            // 先处理_container字段
            if (node.has("_container")) {
                children.setContainer(node.get("_container").asText());
            }

            // 处理数字键的子类别
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                if (!"_container".equals(key)) {
                    try {
                        ChildCategory child = mapper.treeToValue(entry.getValue(), ChildCategory.class);
                        children.getItems().put(key, child);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            return children;
        }

        throw new RuntimeJsonMappingException("Children 必须是对象或空数组");
    }
}
