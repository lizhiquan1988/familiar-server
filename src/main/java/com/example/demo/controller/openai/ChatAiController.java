package com.example.demo.controller.openai;

import com.example.demo.model.chatgpt.Message;
import com.example.demo.service.ChatAI.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/openai")
public class ChatAiController {

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
}
