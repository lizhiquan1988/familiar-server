package com.example.demo.service.ChatAI;

import com.example.demo.model.chatgpt.Message;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class ChatGptService {

    @Value("${openai.api-key}")
    private String apiKey;

    @Autowired
    private WebClient openAiWebClient;

    /**
     * text -> speech (返回 raw audio bytes, e.g. mp3/wav depending on api/model)
     * 注意：model名字按你账号权限设置，如 "gpt-4o-mini-tts" 或官方支持的 tts endpoint model
     */
    public byte[] textToSpeech(String text) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o-mini-tts");
        body.put("voice", "alloy");
        body.put("output_format", "mp3");
        body.put("input", text);

        // 返回 byte[]
        return openAiWebClient.post()
                .uri("/audio/speech")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(byte[].class)
                .block(); // 在异步任务里调用是可以阻塞的
    }


    /**
     * 也可以实现 chat 调用 (同步阻塞版本)
     */
    public String askGpt(List<Message> messages) {
        Map<String,Object> body = new HashMap<>();
        body.put("model", "gpt-4o-mini");
        body.put("messages", messages);

        Map resp = openAiWebClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // 解析 response -> choices[0].message.content
        Object choices = resp.get("choices");
        if (choices instanceof List && !((List) choices).isEmpty()) {
            Map c0 = (Map) ((List) choices).get(0);
            Map message = (Map) c0.get("message");
            return message.get("content").toString();
        }
        return "";
    }

//    public String askGpt(List<Message> questions) {
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(apiKey);
//
//        Map<String, Object> body = new HashMap<>();
//        body.put("model", "gpt-4o-mini");
//        body.put("messages", questions);
//
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
//
//        Map response = restTemplate.postForObject(API_URL, request, Map.class);
//
//        // 解析返回
//        return ((Map)((Map)((List)response.get("choices")).get(0)).get("message")).get("content").toString();
//    }
}

