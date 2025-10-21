package com.example.demo.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class JapanLocalTime {
    public static LocalDateTime getJapanNowTime() {
        return ZonedDateTime.now(ZoneId.of("Asia/Tokyo")).toLocalDateTime();
    }

    public static Long getJapanNowTimestampSeconds() {
        return ZonedDateTime.now(ZoneId.of("Asia/Tokyo"))
                .toInstant()
                .toEpochMilli();
    }

}
