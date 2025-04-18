package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class CreateApiUrlService {

    private static final String BASE_URL = "https://shopping.yahooapis.jp/ShoppingWebService/V3/itemSearch";
    private static final String GENRE_URL = "https://shopping.yahooapis.jp/ShoppingWebService/V1/json/categorySearch";
    private static final Logger logger = LoggerFactory.getLogger(CreateApiUrlService.class);
    public String buildUrl(String appId, String janCode, int startPosition, int resultsPerPage, int categoryId) {
        return UriComponentsBuilder.fromHttpUrl(BASE_URL)
//                .path(endpoint)
                .queryParam("appid", appId)
                .queryParam("image_size", 600)
                .queryParam("jan_code", janCode)
                .queryParam("startPosition", startPosition)
                .queryParam("results", resultsPerPage)
                .queryParam("genre_category_id", categoryId)
                .toUriString();
    }

    public String buildQueryUrl(String appId, String queryStr, int startPosition, int resultsPerPage, int categoryId) {
        try {
            logger.info("queryStr from wechat: {}", queryStr);
            // 构建 URL
            String uriStr = "";
            if (queryStr.isEmpty()) {
                uriStr = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                        .queryParam("appid", appId)
                        .queryParam("image_size", 600)
                        .queryParam("results", resultsPerPage)
                        .queryParam("startPosition", startPosition)
                        .queryParam("genre_category_id", categoryId)
                        .toUriString();
            } else {
                uriStr = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                        .queryParam("appid", appId)
                        .queryParam("image_size", 600)
                        .queryParam("query", queryStr)
                        .queryParam("results", resultsPerPage)
                        .queryParam("startPosition", startPosition)
                        .queryParam("genre_category_id", categoryId)
                        .toUriString();
            }

            logger.info("uriQueryStr: {}", uriStr);
            return replaceQueryParam(uriStr,"query", queryStr);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode query string", e);
        }
    }

    public String buildUrlByGenre(String appId, int startPosition, int resultsPerPage, int categoryId) {
        return UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("appid", appId)
                .queryParam("image_size", 600)
                .queryParam("startPosition", startPosition)
                .queryParam("results", resultsPerPage)
                .queryParam("genre_category_id", categoryId)
                .toUriString();
    }

    public String buildUrlByCategory(String appId, int categoryId) {
        return UriComponentsBuilder.fromHttpUrl(GENRE_URL)
                .queryParam("appid", appId)
                .queryParam("category_id", categoryId)
                .toUriString();
    }

    public static String replaceQueryParam(String url, String paramName, String newValue) {
        return url.replaceAll("(?<=" + paramName + "=)[^&]*", newValue);
    }
}