package com.example.demo.service;

import com.example.demo.model.GoodsCategoryInfo;
import com.example.demo.model.GoodsDetailInfo;
import com.example.demo.repository.GoodsCategoryInfoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GoodsCategoryInfoService {

    private final GoodsCategoryInfoRepository goodsCategoryInfoRepository;

    public GoodsCategoryInfo findGoodsCategory(int categoryId, int categoryParentId) {
        return goodsCategoryInfoRepository.findGoodsCategoryInfoByCategoryIdAndCategoryParentId(categoryId, categoryParentId);
    }

    public List<GoodsCategoryInfo> findGoodsCategories(int categoryId) {
        return goodsCategoryInfoRepository.findGoodsCategoryInfoByCategoryId(categoryId);
    }

    public void updateGoodsCategory(GoodsCategoryInfo updatedGoodsCategoryInfo) {
        GoodsCategoryInfo goodsCategoryInfo =
                goodsCategoryInfoRepository.findGoodsCategoryInfoByCategoryIdAndCategoryParentId(
                        updatedGoodsCategoryInfo.getCategoryId(),
                        updatedGoodsCategoryInfo.getCategoryParentId()
                );
        goodsCategoryInfo.setCategoryLevel(updatedGoodsCategoryInfo.getCategoryLevel());
        goodsCategoryInfo.setCategoryParentId(updatedGoodsCategoryInfo.getCategoryParentId());
        goodsCategoryInfo.setCategoryId(updatedGoodsCategoryInfo.getCategoryId());
        goodsCategoryInfo.setCategoryName(updatedGoodsCategoryInfo.getCategoryName());
        goodsCategoryInfo.setUpdateTime(LocalDateTime.now());
        goodsCategoryInfoRepository.save(goodsCategoryInfo);
    }

    public void saveGoodsCategory(GoodsCategoryInfo goodsCategoryInfo) {
        goodsCategoryInfo.setCreateTime(LocalDateTime.now());
        goodsCategoryInfo.setUpdateTime(LocalDateTime.now());
        goodsCategoryInfoRepository.save(goodsCategoryInfo);
    }

    public List<GoodsCategoryInfo> getGoodsCategoriesByParentId(int parentId) {
        return goodsCategoryInfoRepository.findGoodsCategoryInfoByParentId(parentId);
    }
}
