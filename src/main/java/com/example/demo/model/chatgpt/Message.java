package com.example.demo.model.chatgpt;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
    private String role;   // "user" or "assistant" or "system"

    private String content;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
