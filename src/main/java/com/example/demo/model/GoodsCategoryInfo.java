package com.example.demo.model;

import com.example.demo.config.GoodsCategoryId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@IdClass(GoodsCategoryId.class)
@Table(name = "goods_category_info")
public class GoodsCategoryInfo {
    @Id
    @Column(name = "category_id", nullable = false, length = 10)
    private int categoryId;

    @Id
    @Column(name = "category_parent_id", nullable = false, length = 10)
    private int categoryParentId;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "category_level", nullable = false, length = 100)
    private int categoryLevel;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime; // 直接使用 LocalDateTime

    @Column(name = "update_time")
    private LocalDateTime updateTime; // 直接使用 LocalDateTime
}

