package com.example.demo.config;

import com.example.demo.utils.JapanLocalTime;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Setter
@Getter
public class SensorCache {
    private double temperature;
    private double humidity;
    private LocalDateTime timestamp;

    // 上次存入資料庫的數據
    private double lastSavedTemp = Double.NaN;
    private double lastSavedHum = Double.NaN;
    private LocalDateTime lastSavedTime = null;

    // 閾值
    private static final double TEMP_THRESHOLD = 0.5;
    private static final double HUM_THRESHOLD = 2.0;

    public boolean shouldSave() {
        if (lastSavedTime == null) return true; // 第一次存
        if (lastSavedTime.plusHours(1).isBefore(JapanLocalTime.getJapanNowTime()))
            return true; // 超過1小時
        if (Math.abs(temperature - lastSavedTemp) >= TEMP_THRESHOLD) return true;
        return Math.abs(humidity - lastSavedHum) >= HUM_THRESHOLD;
    }

    public void updateLastSaved() {
        lastSavedTemp = temperature;
        lastSavedHum = humidity;
        lastSavedTime = JapanLocalTime.getJapanNowTime();
    }
}
