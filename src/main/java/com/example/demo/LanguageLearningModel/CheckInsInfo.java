package com.example.demo.LanguageLearningModel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Array;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "checkins_info")
public class CheckInsInfo {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer _id;

    @Column(name = "open_id", nullable = false, length = 50)
    private String openId;

    @Column(name = "checked_days")
    private String checkedDays;

    @Column(name = "year_month_value", length = 6)
    private int yearMonthValue;

    @Column(name = "create_time", updatable = false)
    private Date createTime; // 直接使用 LocalDateTime

    @Column(name = "update_time")
    private Date updateTime; // 直接使用 LocalDateTime
}
