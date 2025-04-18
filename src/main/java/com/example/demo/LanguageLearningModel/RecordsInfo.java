package com.example.demo.LanguageLearningModel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "records_info")
public class RecordsInfo {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer _id;

    @Column(name = "open_id", nullable = false, length = 50)
    private String openId;

    @Column(name = "audio_url", length = 30)
    private String audioUrl;

    @Column(name = "score", length = 3)
    private int score;

    @Column(name = "word_id", length = 100)
    private String wordId;

    @Column(name = "search_date", nullable = false, length = 10)
    private String searchDate;

    @Column(name = "create_time", updatable = false)
    private Date createTime; // 直接使用 LocalDateTime

    @Column(name = "update_time")
    private Date updateTime; // 直接使用 LocalDateTime
}
