package com.example.demo.service.ChatAI;

import com.example.demo.Component.GlobalState;
import com.example.demo.model.chatgpt.Message;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Getter
@Service
public class ChatHistoryService {

    private List<Message> history = new ArrayList<>();

    private final GlobalState globalState;

    // 最大履歴数
    private static final int MAX_TURNS = 10;

    public ChatHistoryService(GlobalState globalState) {
        this.globalState = globalState;
    }

    public void clearMessage() {
        history = new ArrayList<>();
    }

    public void addMessage(Message message) {
        if (history.isEmpty()) {
            globalState.getLanguageName();// TODO 切换语言备用
            Message first =  new Message("system",
                    "You are a home smart speaker. Please answer briefly in Japanese.");
            history.add(first);
        }
        history.add(message);
        if (history.size() > MAX_TURNS * 2) { // user+assistantで2件
            summarizeOldHistory();
        }
    }

    // 古い履歴を要約
    private void summarizeOldHistory() {
        List<Message> oldMessages = new ArrayList<>(history.subList(0, history.size() - MAX_TURNS * 2));

        StringBuilder sb = new StringBuilder();
        for (Message m : oldMessages) {
            sb.append(m.getRole()).append(": ").append(m.getContent()).append("\n");
        }

        String summary = "過去の会話要約: " + sb.substring(0, Math.min(200, sb.length()));

        // 履歴をクリアして、要約をsystemとして残す
        history.clear();
        history.add(new Message("system", summary));
    }
}

