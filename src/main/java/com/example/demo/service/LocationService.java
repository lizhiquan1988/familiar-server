package com.example.demo.service;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.model.LocationInfo;
import com.example.demo.repository.LocationInfoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationService {
    private final LocationInfoRepository locationInfoRepository;

    public JsonNode create(String userId, Double latitude, Double longtitude) {
        LocationInfo location = new LocationInfo();
        location.setUserId(userId);
        location.setLastFlag("1");
        location.setLatitude(latitude);
        location.setLongitude(longtitude);
//        location.setUpdateTime(formatNowTime());
        locationInfoRepository.save(location);
       
        JsonNode rtn = change2Json(userId, "1");

        return rtn;
    }
    public JsonNode updateLocation(String userId, 
    		Double latitude,
    		Double longtitude) {
    	int kns = locationInfoRepository.updateLocation(userId, latitude, longtitude);
       
        JsonNode rtn = change2Json(userId, String.valueOf(kns));

        return rtn;
    }
    
    
    public JsonNode change2Json(String userId, String result) {
        String sjson = "{\"userId\": "+userId+", \"result\": "+result+"}"; 
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = null;
		try {
			json = objectMapper.readTree(sjson);
		} catch (JsonProcessingException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			
		}
        return json;
    }
    
    private String formatNowTime() {
    	// 現在日時を取得
		LocalDateTime date1 = LocalDateTime.now();
		// 表示形式を指定
		DateTimeFormatter dtformat = 
		DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    	return dtformat.format(date1); 
    }
}

