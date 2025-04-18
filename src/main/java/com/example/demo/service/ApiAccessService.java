package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.response.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
@Transactional
public class ApiAccessService {

    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode jsonNode;

    private final GoodsDetailInfoService goodsDetailInfoService;

    private static final Logger logger = LoggerFactory.getLogger(ApiAccessService.class);

    private final GoodsCategoryInfoService goodsCategoryInfoService;

    public JsonNode callApi(String url, String openId) {
        logger.info("url: {}", url);
        // 限流，每秒只能发送一次请求
        rateLimiter.acquire();

        // 发送请求
        String response = restTemplate.getForObject(url, String.class);

        // 把有图的商品放到前面
//        response = sortResponse(response);

        // 解析JSON响应
        try {
            jsonNode = objectMapper.readTree(response);
            saveGoodsDetail(jsonNode, openId);
            // 提取指定字段并构建新的 JsonNode
            return this.extractFields(jsonNode.get("hits"), "name", "code", "price","index", "janCode", "image");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }

    public JsonNode callGenreApi(String url, int categoryParentId, int level) {
        logger.info("url: {}", url);
        // 限流，每秒只能发送一次请求
        rateLimiter.acquire();

        // 发送请求
        String response = restTemplate.getForObject(url, String.class);

        // 解析JSON响应
        try {
            jsonNode = objectMapper.readTree(response);
            saveGoodsCategory(response);
            return jsonNode;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }

    /**
     * 从 JsonNode 中提取指定字段并构建新的 JsonNode
     *
     * @param originalArray 原始 JsonNode
     * @param fields       需要提取的字段名
     * @return 包含指定字段的新 JsonNode
     */
    private JsonNode extractFields(JsonNode originalArray, String... fields) {
        // 创建 ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // 创建新的 ArrayNode
        ArrayNode newArray = objectMapper.createArrayNode();

        // 遍历原始数组中的每个对象
        for (JsonNode originalNode : originalArray) {
            // 创建新的 ObjectNode
            ObjectNode newNode = objectMapper.createObjectNode();

            // 遍历字段并添加到新节点
            for (String field : fields) {
                if (originalNode.has(field)) {
                    newNode.set(field, originalNode.get(field));
                }
            }

            // 将新节点添加到新数组中
            newArray.add(newNode);
        }

        return newArray;
    }

    private void saveGoodsCategory(String jsonData) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ApiResponse apiResponse = objectMapper.readValue(jsonData, ApiResponse.class);

            Current currentCategory = apiResponse.getResultSet().getZero().getResult().getCategories().getCurrent();
            int categoryIdLocal = Integer.parseInt(currentCategory.getId());
            int categoryParentIdLocal = Integer.parseInt(currentCategory.getParentId());
            Title title = currentCategory.getTitle();
            String categoryName = title.getShortTitle();

            final int[] categoryLevel = {1};
            apiResponse.getResultSet().getZero().getResult().getCategories().getCurrent().getPath().getPathItems().forEach((key,path) -> {
                if (path.getAttributes().getDepth() > categoryLevel[0]) {
                    categoryLevel[0] = path.getAttributes().getDepth();
                }
            });

            GoodsCategoryInfo goodsCategoryInfo = new GoodsCategoryInfo();
            goodsCategoryInfo.setCategoryId(categoryIdLocal);
            goodsCategoryInfo.setCategoryParentId(categoryParentIdLocal);
            goodsCategoryInfo.setCategoryLevel(categoryLevel[0]);
            goodsCategoryInfo.setCategoryName(categoryName);
            if (goodsCategoryInfoService.findGoodsCategory(categoryIdLocal, categoryParentIdLocal) == null) {
                goodsCategoryInfoService.saveGoodsCategory(goodsCategoryInfo);
            }

            apiResponse.getResultSet().getZero().getResult().getCategories().getChildren().getItems().forEach((key,child) -> {
                int childCategoryIdLocal = Integer.parseInt(child.getId());
                String childCategoryName = child.getTitle().getShortTitle();
                int childCategoryLevel = categoryLevel[0] + 1;
                GoodsCategoryInfo goodsCategoryChildInfo = new GoodsCategoryInfo();
                goodsCategoryChildInfo.setCategoryId(childCategoryIdLocal);
                goodsCategoryChildInfo.setCategoryParentId(categoryIdLocal);
                goodsCategoryChildInfo.setCategoryLevel(childCategoryLevel);
                goodsCategoryChildInfo.setCategoryName(childCategoryName);
                if (goodsCategoryInfoService.findGoodsCategory(childCategoryIdLocal, categoryIdLocal) == null) {
                    goodsCategoryInfoService.saveGoodsCategory(goodsCategoryChildInfo);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to save goods detail", e);
        }
    }


    private void saveGoodsDetail(JsonNode jsonNode, String openId) {
        try {
//            logger.info("Saving goods detail info"+ jsonNode);
            JsonNode goodsDetailNode = jsonNode.get("hits");
            // 检查是否是数组
//            logger.info("Goods detail is a array: "+ goodsDetailNode.isArray());
            if (goodsDetailNode.isArray()) {
                // 遍历数组
                for (JsonNode details : goodsDetailNode) {
//                    logger.info("Saving goods detail info details is"+ details);
                    String goodsNo = ((details.get("code").asText()).isEmpty()) ? openId : details.get("code").asText();
                    GoodsDetailInfo goodsDetailInfo = new GoodsDetailInfo();
                    goodsDetailInfo.setGoodsNo(goodsNo);
                    goodsDetailInfo.setOpenId(openId);
                    goodsDetailInfo.setGoodsName(details.get("name").asText());
                    goodsDetailInfo.setGoodsPrice(details.get("price").asDouble());
                    goodsDetailInfo.setDescription(details.get("description").asText());
                    JsonNode thumbnailNode = details.get("image");
                    String thumbnailUrl = "";
                    if (thumbnailNode != null && thumbnailNode.has("small")) {
                        if (!thumbnailNode.get("small").asText().isEmpty()) {
                            thumbnailUrl = thumbnailNode.get("small").asText();
//                            thumbnailUrl = downloadImage(thumbnailNode.get("small").asText(), goodsDetailInfo.getGoodsNo()+"thumbnail");
                        }
                    }
                    goodsDetailInfo.setThumbnailUrl(thumbnailUrl);
                    goodsDetailInfo.setGoodsDetailFlag(0);
                    goodsDetailInfo.setFavoriteFlag(0);

                    JsonNode imageUrls = details.get("exImage");
                    String targetPath = "no image";
                    if (imageUrls != null && imageUrls.has("url")) {
                        if (!imageUrls.get("url").asText().isEmpty()) {
//                            targetPath = downloadImage(imageUrls.get("url").asText(), goodsDetailInfo.getGoodsNo());
                            targetPath = imageUrls.get("url").asText();
                        }
                    }
                    goodsDetailInfo.setImageUrl(targetPath);
                    logger.info("targetPath is " + targetPath);
                    if (goodsDetailInfoService.checkExisted(goodsNo, openId) == 0) {
                        goodsDetailInfoService.saveGoodsDetailInfo(goodsDetailInfo);
                    } else {
                        goodsDetailInfoService.updateGoodsDetail(goodsDetailInfo);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save goods detail", e);
        }
    }

    private String downloadImage(String imageUrl, String dynamicValue) {
        // 创建目标目录
        String baseDir = "/var/www/images/goodsImages/";
        Path targetDir = Paths.get(baseDir, dynamicValue);

        try {
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
                logger.info("Created target directory: " + targetDir);
            }
        } catch (IOException e) {
            logger.error("Failed to create target directory: " + targetDir, e);
            return null;
        }

//        logger.info("Downloading image from " + imageUrl);
//        logger.info("Uploading image to " + targetDir);

        // 从URL获取图片
        try {
            URL url = new URL(imageUrl);
            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            Path targetPath = targetDir.resolve(fileName + ".webp");

//            logger.info("Uploading fileName is " + fileName);
//            logger.info("Uploading file's targetPath is " + targetPath);

            // 下载并保存图片
            try (InputStream in = url.openStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Image successfully saved to: " + targetPath);
                return targetPath.toString();
            } catch (IOException e) {
                logger.error("Failed to copy image from " + imageUrl, e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Failed to process image URL: " + imageUrl, e);
            return null;
        }
    }
//
//    private String sortResponse(String jsonStr) {
//        try {
//            // 创建 ObjectMapper 对象
//            ObjectMapper objectMapper = new ObjectMapper();
//
//            // 解析 JSON 字符串
//            JsonNode rootNode = objectMapper.readTree(jsonStr);
//
//            // 获取 hits 数组
//            ArrayNode hitsArray = (ArrayNode) rootNode.path("hits");
//
//            // 将 hits 数组转换为 List
//            List<JsonNode> hitsList = new ArrayList<>();
//            hitsArray.forEach(hitsList::add);
//
//            // 按照 exImage 是否存在进行排序
//            Collections.sort(hitsList, new Comparator<JsonNode>() {
//                @Override
//                public int compare(JsonNode o1, JsonNode o2) {
//                    boolean hasExImage1 = o1.has("exImage");
//                    boolean hasExImage2 = o2.has("exImage");
//
//                    // 有 exImage 的排在前面
//                    if (hasExImage1 && !hasExImage2) {
//                        return -1;
//                    } else if (!hasExImage1 && hasExImage2) {
//                        return 1;
//                    } else {
//                        return 0;
//                    }
//                }
//            });
//
//            // 将排序后的 List 重新转换为 ArrayNode
//            ArrayNode sortedHitsArray = objectMapper.createArrayNode();
//            hitsList.forEach(sortedHitsArray::add);
//
//            // 将排序后的 hits 数组替换回原始 JSON
//            ((com.fasterxml.jackson.databind.node.ObjectNode) rootNode).set("hits", sortedHitsArray);
//
//            // 将排序后的 JSON 转换为字符串
//            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
//        } catch (Exception e) {
//            logger.error(e.toString());
//            return null;
//        }
//    }
}
