package com.example.demo.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.LocationInfo;
import com.example.demo.repository.LocationInfoRepository;
import com.example.demo.service.LocationService;
import com.example.demo.service.UserInfoService;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "https://www.mimamaori.tech")
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class MysqlDataController {
  @Autowired
  LocationInfoRepository locationInfoRepository;
  
  private final LocationService locationService;
  
  private final UserInfoService userInfoService;
  
  @GetMapping("/location")
  public ResponseEntity<List<LocationInfo>> getLocationInfo(
		  @RequestParam(required = true) String userId, 
		  @RequestParam(required = true) String lastFlag) {
	  List<LocationInfo> list = locationInfoRepository.findByUserIdAndLastFlag(userId, lastFlag);
    return new ResponseEntity<List<LocationInfo>>(list, HttpStatus.OK);
  }
  @GetMapping("/save")
  @PostMapping
  public String saveLocation(
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
  
  @PostMapping("/api/register")
  public String registerUser(HttpServletRequest request) {
      ServletInputStream is = null;
      try {
          is = request.getInputStream();
          StringBuilder sb = new StringBuilder();
          byte[] buf = new byte[1024];
          int len = 0;
          while ((len = is.read(buf)) != -1) {
              sb.append(new String(buf, 0, len));
          }
          System.out.println(sb.toString());
          return userInfoService.registerUser(sb.toString());
      } catch (IOException e) {
          e.printStackTrace();
      } finally {
          try {
              if (is != null) {
                  is.close();
              }
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
      return null;
  }
  

  @PostMapping("/api/login")
  public String userLogin(HttpServletRequest request) {
      ServletInputStream is = null;
      try {
          is = request.getInputStream();
          StringBuilder sb = new StringBuilder();
          byte[] buf = new byte[1024];
          int len = 0;
          while ((len = is.read(buf)) != -1) {
              sb.append(new String(buf, 0, len));
          }
          System.out.println(sb.toString());
          return userInfoService.getUserInfo(sb.toString());
      } catch (IOException e) {
          e.printStackTrace();
      } finally {
          try {
              if (is != null) {
                  is.close();
              }
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
      return null;
  }
  
  
  @GetMapping("/noDblocation")
  public ResponseEntity<List<LocationInfo>> getLocationInfoFromData(
		  @RequestParam(required = true) String userId, 
		  @RequestParam(required = true) String lastFlag) {
	  LocationInfo locationInfo = new LocationInfo();
	  locationInfo.setLatitude(LocationData.getLatitude());
	  locationInfo.setLongitude(LocationData.getLongitude());
	  locationInfo.setUpdateTime(formatTime());
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
  private String formatTime() {
	  Date exDate = new Date();
      SimpleDateFormat exDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:MM:SS");
      return exDateFormat.format(exDate);
  }
}