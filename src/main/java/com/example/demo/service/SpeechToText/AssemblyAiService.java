package com.example.demo.service.SpeechToText;

import com.example.demo.Component.GlobalState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AssemblyAiService {

    @Value("${assemblyai.api.key}")
    private String apiKey;

    private final GlobalState globalState;

    private final String UPLOAD_URL = "https://api.assemblyai.com/v2/upload";
    private final String TRANSCRIBE_URL = "https://api.assemblyai.com/v2/transcript";

    private final RestTemplate restTemplate = new RestTemplate();

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public AssemblyAiService(GlobalState globalState) {
        this.globalState = globalState;
    }

    // 上传音频文件 (可以用字节流)
    public String uploadAudio(byte[] audioData) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(audioData, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                UPLOAD_URL,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );
        Map<String, Object> body = response.getBody();
        if (body != null && body.containsKey("upload_url")) {
            return (String) body.get("upload_url");
        } else {
            throw new RuntimeException("Upload failed: " + response);
        }
    }

    // 提交转写任务
    public String requestTranscription(String audioUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        int languageFlag = globalState.getValue();
        String lang = (languageFlag == 0) ? "zh" : "ja";

        Map<String, Object> body = new HashMap<>();
        body.put("audio_url", audioUrl);
        body.put("language_code", lang);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                TRANSCRIBE_URL,
                requestEntity,
                Map.class
        );

        return (String) response.getBody().get("id");
    }

    // 获取转写结果
    public Map<String, Object> getTranscriptionResult(String transcriptId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                TRANSCRIBE_URL + "/" + transcriptId,
                HttpMethod.GET,
                requestEntity,
                Map.class
        );

        return response.getBody();
    }
}

