package com.example.demo.Component;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GlobalState {
    private final AtomicInteger languageCode = new AtomicInteger(0); // 0=中文, 1=日文

    public int getValue() {
        return languageCode.get();
    }

    public void setValue(int value) {
        languageCode.set(value);
    }

    public String getLanguageName() {
        return languageCode.get() == 0 ? "Chinese" : "Japanese";
    }
}




