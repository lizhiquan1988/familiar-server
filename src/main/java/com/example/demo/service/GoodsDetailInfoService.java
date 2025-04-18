package com.example.demo.service;

import com.example.demo.model.GoodsDetailInfo;
import com.example.demo.repository.GoodsDetailInfoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class GoodsDetailInfoService {

    private final GoodsDetailInfoRepository goodsDetailInfoRepository;


    public GoodsDetailInfo getGoodsDetailInfo(String goodsNo, String openId) {
        return goodsDetailInfoRepository.findByGoodsNoAndOpenId(goodsNo, openId);
    }

    public GoodsDetailInfo getGoodsDetailInfoHistory(String goodsNo, String openId) {
        return goodsDetailInfoRepository.findByGoodsNoAndOpenIdAndGoodsDetailFlag(goodsNo, openId, 1);
    }

    public GoodsDetailInfo getFavoriteGoods(String goodsNo, String openId) {
        return goodsDetailInfoRepository.findByGoodsNoAndOpenIdAndFavoriteFlag(goodsNo, openId, 1);
    }

    public void updateGoodsDetail(GoodsDetailInfo updatedGoodsDetailInfo) {
        GoodsDetailInfo goodsDetailInfo =
                goodsDetailInfoRepository.findByGoodsNoAndOpenId(
                        updatedGoodsDetailInfo.getGoodsNo(),
                        updatedGoodsDetailInfo.getOpenId()
                );
        goodsDetailInfo.setGoodsName(updatedGoodsDetailInfo.getGoodsName());
        goodsDetailInfo.setGoodsPrice(updatedGoodsDetailInfo.getGoodsPrice());
        goodsDetailInfo.setDescription(updatedGoodsDetailInfo.getDescription());
        goodsDetailInfo.setGoodsPrice(updatedGoodsDetailInfo.getGoodsPrice());
        goodsDetailInfo.setImageUrl(updatedGoodsDetailInfo.getImageUrl());
        goodsDetailInfo.setGoodsDetailFlag(updatedGoodsDetailInfo.getGoodsDetailFlag());
        goodsDetailInfo.setFavoriteFlag(updatedGoodsDetailInfo.getFavoriteFlag());
        goodsDetailInfo.setUpdateTime(LocalDateTime.now());
        goodsDetailInfoRepository.save(goodsDetailInfo);
    }

    public int checkExisted(String goodsNo,String openId) {
        GoodsDetailInfo goodsDetailInfo = goodsDetailInfoRepository.findByGoodsNoAndOpenId(goodsNo, openId);
        if (goodsDetailInfo == null) {
            return 0;
        }
        return 1;
    }

    public void saveGoodsDetailInfo(GoodsDetailInfo goodsDetailInfo) {
        goodsDetailInfo.setCreateTime(LocalDateTime.now());
        goodsDetailInfo.setUpdateTime(LocalDateTime.now());
        goodsDetailInfoRepository.save(goodsDetailInfo);
    }

    public int updateGoodsDetailFlagByGoodsNoAndOpenId(int goodsDetailFlag, String goodsNo, String openId) {
        return goodsDetailInfoRepository.updateGoodsDetailFlagByGoodsNoAndOpenId(goodsDetailFlag, goodsNo, openId);
    }

    public int updateFavoriteFlagByGoodsNoAndOpenId(int favoriteFlag, String goodsNo, String openId) {
        return goodsDetailInfoRepository.updateFavoriteFlagByGoodsNoAndOpenId(favoriteFlag, goodsNo, openId);
    }
}
