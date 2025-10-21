package com.example.demo.service.TexttoSpeech;

import com.example.demo.Component.GlobalState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class TextToSpeechService {
    private final String VIOCEVOX_BASE_URL = "http://localhost:50021";

    private final RestTemplate restTemplate = new RestTemplate();

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private static String downloadPlayVoiceUrl = "https://www.mimamaori.tech/images/audio/tts/output.wav";

    private final GlobalState globalState;

    public TextToSpeechService(GlobalState globalState) {
        this.globalState = globalState;
    }

    public String speak(String text) {
//        if (globalState.getValue() == 0) {
//            return new byte[0];
//        } else {
//            return voiceVoxSpeak(text);
//        }
        return voiceVoxSpeak(text);
    }

    public String voiceVoxSpeak(String text) {
        // 1. 请求 VOICEVOX engine audio_query
        try {
            String queryUrl = VIOCEVOX_BASE_URL + "/audio_query?text=" + text + "&speaker=2";
            String audioQuery = restTemplate.postForObject(queryUrl, null, String.class);

            logger.info("开始文字转语音处理");
            // 2. 请求 synthesis
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(audioQuery, headers);

            String synthUrl = "http://localhost:50021/synthesis?speaker=1";
            byte[] audioData = restTemplate.postForObject(synthUrl, entity, byte[].class);

            if (audioData == null || audioData.length == 0) {
                logger.info("没有音频返回");
            }
            // 3. 保存为文件
            String filePath = "/www/wwwroot/myapp/static/audio/tts/output.wav";
            if (audioData != null) {
                Files.write(Paths.get(filePath), audioData);
                logger.info("音频文件保存成功");
                return downloadPlayVoiceUrl;
            } else {
                logger.info("音频文件生产失败");
                return null;
            }
        } catch (Exception exception) {
            logger.error("日语语音生成时报错：${}", exception.getMessage(), exception);
            return null;
        }
    }
}
