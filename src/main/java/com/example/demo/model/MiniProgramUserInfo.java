package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.repository.Temporal;

import java.time.LocalDateTime;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "mini_program_user_info")
public class MiniProgramUserInfo {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer num;

    @Column(name = "open_id", nullable = false, length = 50)
    private String openId;

    @Column(name = "user_level_desc", nullable = false, length = 30)
    private String userLevelDesc;

    @Column(name = "user_id", nullable = false, length = 10)
    private String userId;

    @Column(name = "register_date", nullable = true, length = 30)
    private String registerDate;

    @Column(name = "avatar_url", nullable = true, length = 200)
    private String avatarUrl;

    @Column(name = "nick_name", nullable = false, length = 10)
    private String nickName;

    @Column(name = "create_time", updatable = false)
    private Date createTime; // 直接使用 LocalDateTime

    @Column(name = "update_time")
    private Date updateTime; // 直接使用 LocalDateTime
}
