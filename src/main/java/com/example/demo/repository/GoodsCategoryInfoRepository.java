package com.example.demo.repository;

import com.example.demo.model.GoodsCategoryInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GoodsCategoryInfoRepository  extends JpaRepository<GoodsCategoryInfo, Integer> {
    GoodsCategoryInfo findGoodsCategoryInfoByCategoryIdAndCategoryParentId(int categoryId, int categoryParentId);

    List<GoodsCategoryInfo> findGoodsCategoryInfoByCategoryId(int categoryId);

    @Query("SELECT u FROM GoodsCategoryInfo u WHERE u.categoryParentId=?1 ORDER BY u.createTime")
    List<GoodsCategoryInfo> findGoodsCategoryInfoByParentId(int parentId);
}
