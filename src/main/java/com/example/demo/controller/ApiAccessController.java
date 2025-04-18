package com.example.demo.controller;

import com.example.demo.service.ApiAccessService;
import com.example.demo.service.CreateApiUrlService;
import com.example.demo.utils.ApiAppId;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mini/api/")
public class ApiAccessController {

    @Autowired
    private CreateApiUrlService createApiUrlService;

    @Autowired
    private ApiAccessService apiAccessService;

    @GetMapping("/yahoo-goods-info")
    public JsonNode callExternalApi(@RequestParam String janCode, @RequestParam String openId,
                                    @RequestParam int startPosition, @RequestParam int resultsPerPage,
                                    @RequestParam(required = false) Integer categoryId) {
        String url = createApiUrlService.buildUrl(ApiAppId.appId,
                janCode,
                startPosition,
                resultsPerPage,
                categoryId != null ? categoryId : 1);
        return apiAccessService.callApi(url, openId);
    }

    @GetMapping("/yahoo-goods-info/query")
    public JsonNode callExternalApiByQuery(String queryStr, @RequestParam String openId,
                                           @RequestParam int startPosition, @RequestParam int resultsPerPage,
                                           @RequestParam(required = false) Integer categoryId) {
        String url = createApiUrlService.buildQueryUrl(ApiAppId.appId,
                queryStr,
                startPosition,
                resultsPerPage,
                categoryId != null ? categoryId : 1);
        return apiAccessService.callApi(url, openId);
    }

    @GetMapping("/yahoo-goods-info/genre")
    public JsonNode callExternalApiByGenre(@RequestParam String openId,
                                    @RequestParam int startPosition, @RequestParam int resultsPerPage,
                                    @RequestParam Integer categoryId) {
        String url = createApiUrlService.buildUrlByGenre(ApiAppId.appId,
                startPosition,
                resultsPerPage,
                categoryId);
        return apiAccessService.callApi(url, openId);
    }

    @GetMapping("/yahoo-goods-info/genreData/info")
    public JsonNode callExternalApiGetGenreInfo(@RequestParam Integer categoryId,
                                                Integer categoryParentId,
                                                Integer level) {
        String url = createApiUrlService.buildUrlByCategory(ApiAppId.appId, categoryId);
        int categoryParentIdLocal = categoryParentId != null ? categoryParentId : 1;
        int levelLocal = level != null ? level : 1;
        return apiAccessService.callGenreApi(url, categoryParentIdLocal, levelLocal);
    }
}