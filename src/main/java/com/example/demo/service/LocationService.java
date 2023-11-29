package com.example.demo.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.example.demo.model.LocationInfo;
import com.example.demo.repository.LocationInfoRepository;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationService {
    private final LocationInfoRepository locationInfoRepository;
    
    public String create(String userId, Double latitude, Double longtitude) {
        LocationInfo location = new LocationInfo();
        location.setUserId(userId);
        location.setLastFlag("1");
        location.setLatitude(latitude);
        location.setLongitude(longtitude);
        locationInfoRepository.save(location);
        return "OK";
    }
    public String updateLocation(String userId, 
    		Double latitude,
    		Double longtitude) {
    	int kns = locationInfoRepository.updateLocation(userId, latitude, longtitude);
        return String.valueOf(kns);
    }
}

