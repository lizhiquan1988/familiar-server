package com.example.demo.model;

import com.example.demo.utils.JapanLocalTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "MedicineData_info")
public class MedicineDataInfo {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer num;
    private Double distance;
    private LocalDateTime timestamp = JapanLocalTime.getJapanNowTime();
}