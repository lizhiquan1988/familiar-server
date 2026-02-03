package com.example.demo.controller.openai;

import com.example.demo.config.TtsProperties;
import com.example.demo.model.chatgpt.Message;
import com.example.demo.service.ChatAI.ChatHistoryService;
import com.example.demo.utils.JapanLocalTime;
import com.example.demo.websocket.netty.protocol.SendMessageDataForAiSpeaker;
import com.example.demo.websocket.netty.util.SendMessageToFront;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/openai")
public class ChatAiController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TtsProperties ttsProperties;

    @Autowired
    private ChatHistoryService chatHistoryService;

    @GetMapping("/clearHistory")
    public Map<String, Object> clearHistory() {
        List<Message> histories = chatHistoryService.getHistory();
        chatHistoryService.clearMessage();
        return Map.of(
                "clearNumber", histories.size(),
                "result", "OK"
        );
    }

    @GetMapping("/testAudio")
    public Map<String, Object> playTestAudio() {
        SendMessageDataForAiSpeaker sendMessage = new SendMessageDataForAiSpeaker(
                "PLAY_AUDIO",
                JapanLocalTime.getJapanNowTimestampSeconds(),
                "you can over Websocket.",
                0,
                "",
                "https://mimamaori.tech/images/audio/tts/tts_20251110_072924_acfc07cb.mp3");
        SendMessageToFront.sendTo(sendMessage, logger);
        return Map.of(
                "playUrl", "https://mimamaori.tech/images/audio/tts/tts_20251110_072924_acfc07cb.mp3",
                "result", "OK"
        );
    }
}
