package com.example.demo.controller;

import com.example.demo.model.GoodsDetailInfo;
import com.example.demo.service.GoodsDetailInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "https://www.mimamaori.tech")
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class GoodsDetailInfoController {
    private final GoodsDetailInfoService goodsDetailInfoService;

    @GetMapping("/mini/api/goodsDetail")
    public GoodsDetailInfo getGoodsDetailInfo(@RequestParam() String goodsNo, @RequestParam() String openId) {
        return goodsDetailInfoService.getGoodsDetailInfo(goodsNo,openId);
    }

    @GetMapping("/mini/api/goodsDetailHistory")
    public GoodsDetailInfo getGoodsDetailInfoHistory(@RequestParam() String goodsNo, @RequestParam() String openId) {
        return goodsDetailInfoService.getGoodsDetailInfoHistory(goodsNo, openId);
    }

    @GetMapping("/mini/api/favoriteGoods")
    public GoodsDetailInfo getFavoriteGoods(@RequestParam() String goodsNo, @RequestParam() String openId) {
        return goodsDetailInfoService.getFavoriteGoods(goodsNo, openId);
    }

    @GetMapping("/mini/api/setGoodsDetailFlag")
    public int setGoodsDetailFlag(@RequestParam() int goodsDetailFlag, @RequestParam() String goodsNo,@RequestParam() String openId) {
        return goodsDetailInfoService.updateGoodsDetailFlagByGoodsNoAndOpenId(goodsDetailFlag, goodsNo,openId);
    }

    @GetMapping("/mini/api/setFavoriteFlag")
    public int setFavoriteFlag(@RequestParam() int favoriteFlag, @RequestParam() String goodsNo, @RequestParam() String openId) {
        return goodsDetailInfoService.updateFavoriteFlagByGoodsNoAndOpenId(favoriteFlag, goodsNo,openId);
    }
}
