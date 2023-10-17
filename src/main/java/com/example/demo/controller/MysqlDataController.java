package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.LocationInfo;
import com.example.demo.repository.LocationInfoRepository;
import com.example.demo.service.LocationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeCreator;

import lombok.RequiredArgsConstructor;

//@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class MysqlDataController {
  @Autowired
  LocationInfoRepository locationInfoRepository;
  
  private final LocationService locationService;
  
  @GetMapping("/location")
  public ResponseEntity<List<LocationInfo>> getLocationInfo(
		  @RequestParam(required = true) String userId, 
		  @RequestParam(required = true) String lastFlag) {
	  List<LocationInfo> list = locationInfoRepository.findByUserIdAndLastFlag(userId, lastFlag);
    return new ResponseEntity<List<LocationInfo>>(list, HttpStatus.OK);
  }
  @GetMapping("/save")
  @PostMapping
  public JsonNode saveLocation(
		  @RequestParam(required = true)String userId,
		  @RequestParam(required = true)String latitude, 
		  @RequestParam(required = true)String longtitude) {
	  List<LocationInfo> list = locationInfoRepository.findByUserIdAndLastFlag(userId, "1");
	  if(list.size() > 0) {
		  return locationService.updateLocation(userId, Double.valueOf(latitude), Double.valueOf(longtitude));
	  } else {
		  return locationService.create(userId, Double.valueOf(latitude), Double.valueOf(longtitude));
	  }
  }
  
  @GetMapping("/noDblocation")
  public ResponseEntity<List<LocationInfo>> getLocationInfoFromData(
		  @RequestParam(required = true) String userId, 
		  @RequestParam(required = true) String lastFlag) {
	  LocationInfo locationInfo = new LocationInfo();
	  locationInfo.setLatitude(LocationData.getLatitude());
	  locationInfo.setLongitude(LocationData.getLongitude());
	  List<LocationInfo> list = new ArrayList<LocationInfo>();
	  list.add(locationInfo);
    return new ResponseEntity<List<LocationInfo>>(list, HttpStatus.OK);
  }
  @GetMapping("/noDbsave")
  @PostMapping
  public String saveLocationToData(
		  @RequestParam(required = true)String userId,
		  @RequestParam(required = true)String latitude, 
		  @RequestParam(required = true)String longtitude) {
	  LocationData.setLatitude(Double.valueOf(latitude));
	  LocationData.setLongitude(Double.valueOf(longtitude));
	  return "OK";
	  
  }
}