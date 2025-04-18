package com.example.demo.LanguageLearningModel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

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
    private Date createTime; // 直接使用 LocalDateTime

    @Column(name = "update_time")
    private Date updateTime; // 直接使用 LocalDateTime
}
