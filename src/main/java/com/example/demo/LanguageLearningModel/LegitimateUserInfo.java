package com.example.demo.LanguageLearningModel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "legitimate_user_info")
public class LegitimateUserInfo {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer _id;

    @Column(name = "student_grade", nullable = false, length = 8)
    private int studentGrade;

    @Column(name = "student_name", nullable = false, length = 200)
    private String studentName;

    @Column(name = "status", nullable = false, length = 1)
    private int status;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime; // 直接使用 LocalDateTime

    @Column(name = "update_time")
    private LocalDateTime updateTime; // 直接使用 LocalDateTime
}
