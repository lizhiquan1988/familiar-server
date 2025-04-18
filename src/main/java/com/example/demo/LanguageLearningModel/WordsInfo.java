package com.example.demo.LanguageLearningModel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "words_info")
public class WordsInfo {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer _id;

    @Column(name = "open_id", nullable = false, length = 50)
    private String openId;

    @Column(name = "audio_url", length = 30)
    private String audioUrl;

    @Column(name = "category", nullable = false, length = 6)
    private int category;

    @Column(name = "chinese", nullable = true, length = 100)
    private String chinese;

    @Column(name = "difficulty", nullable = true, length = 1)
    private int difficulty;

    @Column(name = "english", nullable = true, length = 100)
    private String english;

    @Column(name = "phonetic", nullable = true, length = 30)
    private String phonetic;

    @Column(name = "search_date", nullable = false, length = 10)
    private String searchDate;

    @Column(name = "create_time", updatable = false)
    private Date createTime; // 直接使用 LocalDateTime

    @Column(name = "update_time")
    private Date updateTime; // 直接使用 LocalDateTime
}
