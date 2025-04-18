package com.example.demo.config;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import java.util.Objects;

@Getter
@Setter
public class GoodsCategoryId implements Serializable {
    // getters 和 setters
    private int categoryId;
    private int categoryParentId;

    // 必须有无参构造函数
    public GoodsCategoryId() {}

    // 可以有带参数的构造函数（可选）
    public GoodsCategoryId(int categoryId, int parentId) {
        this.categoryId = categoryId;
        this.categoryParentId = parentId;
    }

    // 必须实现 equals 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GoodsCategoryId that = (GoodsCategoryId) o;

        return Objects.equals(categoryId, that.categoryId) &&
                Objects.equals(categoryParentId, that.categoryParentId);
    }

    // 必须实现 hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(categoryId, categoryParentId);
    }

    // 可选：实现 toString 方法（方便调试）
    @Override
    public String toString() {
        return "GoodsCategoryId{" +
                "categoryId=" + categoryId +
                ", parentId=" + categoryParentId +
                '}';
    }
}