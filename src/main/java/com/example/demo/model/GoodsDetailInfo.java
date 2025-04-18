package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "goods_detail_info")
public class GoodsDetailInfo {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer num;

    @Column(name = "goods_no", nullable = false, length = 100)
    private String goodsNo;

    @Column(name = "open_id", nullable = false, length = 100)
    private String openId;

    @Column(name = "goods_name", nullable = false, length = 500)
    private String goodsName;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "image_url", length = 300)
    private String imageUrl;

    @Column(name = "thumbnail_url", length = 300)
    private String thumbnailUrl;

    @Column(name = "goods_price", nullable = false, length = 30)
    private Double goodsPrice;

    @Column(name = "goods_detail_flag", nullable = false, length = 1)
    private int goodsDetailFlag;

    @Column(name = "favorite_falg", nullable = false, length = 1)
    private int favoriteFlag;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime; // 直接使用 LocalDateTime

    @Column(name = "update_time")
    private LocalDateTime updateTime; // 直接使用 LocalDateTime
}

