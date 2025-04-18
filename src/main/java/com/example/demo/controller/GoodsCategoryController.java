package com.example.demo.controller;

import com.example.demo.model.GoodsCategoryInfo;
import com.example.demo.service.GoodsCategoryInfoService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mini/api/")
public class GoodsCategoryController {
    @Autowired
    private GoodsCategoryInfoService goodsCategoryInfoService;

    @GetMapping("/yahoo-goods-info/genreData/FromDB")
    public ResponseEntity<?> callExternalApiGetGenreInfo(@RequestParam Integer categoryId,
                                                         Integer categoryParentId,
                                                         Integer level) {
        int categoryParentIdLocal = categoryParentId != null ? categoryParentId : 1;
        int levelLocal = level != null ? level : 1;
        List<GoodsCategoryInfo> results =  goodsCategoryInfoService.getGoodsCategoriesByParentId(categoryParentIdLocal);
        return ResponseEntity.ok(results);
    }
}
