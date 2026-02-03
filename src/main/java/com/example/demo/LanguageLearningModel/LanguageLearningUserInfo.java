package com.example.demo.LanguageLearningModel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "language_learning_user_info")
public class LanguageLearningUserInfo {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer _id;

    @Column(name = "open_id", nullable = false, length = 50)
    private String openId;

    @Column(name = "user_level_desc", nullable = false, length = 30)
    private String userLevelDesc;

    @Column(name = "register_date", nullable = false, length = 30)
    private String registerDate;

    @Column(name = "last_study_date", nullable = true, length = 30)
    private String lastStudyDate;

    @Column(name = "study_days", nullable = true, length = 8)
    private int studyDays;

    @Column(name = "student_grade", nullable = true, length = 2)
    private int studentGrade;

    @Column(name = "is_admin", nullable = true, length = 1)
    private int isAdmin;

    @Column(name = "avatar_url", nullable = true, length = 200)
    private String avatarUrl;

    @Column(name = "nick_name", nullable = false, length = 10)
    private String nickName;

    @Column(name = "student_name", nullable = false, length = 10)
    private String studentName;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime; // 直接使用 LocalDateTime

    @Column(name = "update_time")
    private LocalDateTime updateTime; // 直接使用 LocalDateTime
}
