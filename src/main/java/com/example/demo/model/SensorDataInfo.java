package com.example.demo.model;

import com.example.demo.utils.JapanLocalTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Setter
@Getter
@Entity
@Table(name = "SensorData_info")
public class SensorDataInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double temperature;
    private Double humidity;
    private LocalDateTime timestamp = JapanLocalTime.getJapanNowTime();
}
