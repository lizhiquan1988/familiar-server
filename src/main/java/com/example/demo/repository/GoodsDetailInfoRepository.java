package com.example.demo.repository;

import com.example.demo.model.GoodsDetailInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface GoodsDetailInfoRepository extends JpaRepository<GoodsDetailInfo, Integer> {
    @Modifying
    @Query("DELETE GoodsDetailInfo u WHERE u.goodsNo=?1")
    int deleteByGoodsNo(String goodsNo);
    GoodsDetailInfo findByGoodsNoAndOpenId(String goodsNo, String openId);

    GoodsDetailInfo findByGoodsNoAndOpenIdAndGoodsDetailFlag(String goodsNo, String openId, int goodsDetailFlag);

    GoodsDetailInfo findByGoodsNoAndOpenIdAndFavoriteFlag(String goodsNo, String openId, int favoriteFlag);

    int deleteByGoodsNoAndOpenId(String goodsNo, String openId);

    @Query("update GoodsDetailInfo g set g.goodsDetailFlag = :goodsDetailFlag where g.goodsNo = :goodsNo and g.openId = :openId")
    @Modifying
    int updateGoodsDetailFlagByGoodsNoAndOpenId(int goodsDetailFlag, String goodsNo, String openId);

    @Query("update GoodsDetailInfo g set g.favoriteFlag = :favoriteFlag where g.goodsNo = :goodsNo and g.openId = :openId")
    @Modifying
    int updateFavoriteFlagByGoodsNoAndOpenId(int favoriteFlag, String goodsNo, String openId);
}
