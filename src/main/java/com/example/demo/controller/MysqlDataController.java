package com.example.demo.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.example.demo.model.MaintainInfo;
import com.example.demo.model.MiniProgramUserInfo;
import com.example.demo.service.MaintainInfoService;
import com.example.demo.service.MiniProgramUserInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private final MiniProgramUserInfoService miniProgramUserInfoService;

  private final MaintainInfoService maintainInfoService;
  private static final Logger logger = LoggerFactory.getLogger(MysqlDataController.class);
  
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
	  if(!list.isEmpty()) {
		  return locationService.updateLocation(userId, Double.valueOf(latitude), Double.valueOf(longtitude));
	  } else {
		  return locationService.create(userId, Double.valueOf(latitude), Double.valueOf(longtitude));
	  }
  }

    @PostMapping("/mini/api/login")
    public String miniProgramUserLogin(HttpServletRequest request) {
        String queryStr = getRequestJsonData(request);
        if (!queryStr.isEmpty()) {
            try {
                MiniProgramUserInfo miniProgramUserInfo = miniProgramUserInfoService.getUserInfo(queryStr);
                if (miniProgramUserInfo != null) {
                    // 提取需要的字段
                    Map<String, Object> jsonMap = new HashMap<>();
                    jsonMap.put("memberNo", miniProgramUserInfo.getNum());
                    jsonMap.put("registerDate", miniProgramUserInfo.getRegisterDate());
                    jsonMap.put("nickName", miniProgramUserInfo.getNickName());
                    jsonMap.put("avatarUrl", miniProgramUserInfo.getAvatarUrl());
                    String token = miniProgramUserInfoService.getUserToken(
                            miniProgramUserInfo.getOpenId(),
                            miniProgramUserInfo.getNickName(),
                            miniProgramUserInfo
                    );
                    jsonMap.put("token", token);
                    // 将 Map 转换为 JSON 字符串
                    ObjectMapper objectMapper = new ObjectMapper();
                    return objectMapper.writeValueAsString(jsonMap);
                } else {
                    return "{\"error\": \"Failed to get user data.\"}";
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } else {
            return "{\"error\": \"Failed to get user data.\"}";
        }

        return "{\"error\": \"Failed to get user data.\"}";
    }

  @PostMapping("/mini/api/register")
  public String registerMiniProgramUser(HttpServletRequest request) {
      String queryStr = getRequestJsonData(request);
      if (!queryStr.isEmpty()) {
          return miniProgramUserInfoService.registerUser(queryStr);
      }
      return null;
  }

    @PostMapping("/mini/api/updateUserInfo")
    public String miniProgramUpdateUserInfo(HttpServletRequest request) {
        String queryStr = getRequestJsonData(request);
        if (!queryStr.isEmpty()) {
            int res = miniProgramUserInfoService.updateUserInfo(queryStr);
            if (res > 0) {
                return "success";
            } else {
                return "{\"error\": \"Failed to update user data.\"}";
            }
        } else {
            return "{\"error\": \"Failed to update user data.\"}";
        }
    }

    @GetMapping("/mini/api/maintain")
    public String getOpenid(HttpServletRequest request) {
        // 传入前端的 code 请求微信 API
        String pageId = request.getParameter("pageId");
        MaintainInfo maintainInfo = maintainInfoService.findAllByPageId(pageId);

        String jsonString = "";
        // 使用 ObjectMapper 转换为 JSON 字符串
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonString = objectMapper.writeValueAsString(maintainInfo);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "{\"error\":\"Failed to get openid\"}";
        }
        return jsonString;
    }

  @PostMapping("/api/register")
  public String registerUser(HttpServletRequest request) {
      String queryStr = getRequestJsonData(request);
      if (!queryStr.isEmpty()) {
          return userInfoService.registerUser(queryStr);
      }
      return null;
  }
  

  @PostMapping("/api/login")
  public String userLogin(HttpServletRequest request) {
      String queryStr = getRequestJsonData(request);
      if (!queryStr.isEmpty()) {
          return userInfoService.getUserInfo(queryStr);
      } else {
          return null;
      }
  }

  private String getRequestJsonData(HttpServletRequest request) {
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
          return sb.toString();
      } catch (IOException e) {
          logger.error(e.getMessage());
          return "";
      } finally {
          try {
              if (is != null) {
                  is.close();
              }
          } catch (IOException e) {
              logger.error(e.getMessage());
          }
      }

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
	  LocationData.setLatitude(Double.parseDouble(latitude));
	  LocationData.setLongitude(Double.parseDouble(longtitude));
	  return "OK";
	  
  }
  private String formatTime() {
	  Date exDate = new Date();
      SimpleDateFormat exDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
      return exDateFormat.format(exDate);
  }
}