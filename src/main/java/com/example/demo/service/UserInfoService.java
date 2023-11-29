package com.example.demo.service;

import java.util.List;
import java.util.Map;

import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.example.demo.model.UserInfo;
import com.example.demo.repository.UserInfoRepository;
import com.example.demo.utils.JwtUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserInfoService {
    private final UserInfoRepository userInfoRepository;

    public String registerUser(String jsonString) {
        UserInfo userInfo = JSONObject.parseObject(jsonString, UserInfo.class);
        userInfo.setFamililyId("fam000001");
//        userInfo.setChildGrade(1);
        List<UserInfo> list = userInfoRepository.findByUserId(userInfo.getUserId());
    	if (list != null && list.size() > 0 ) {
    		return "EXIST";
    	}
    	
        userInfoRepository.save(userInfo);
        return "OK";
    }
    
    @SuppressWarnings("unchecked")
	public String getUserInfo(String jsonString) {
    	UserInfo userInfo = JSONObject.parseObject(jsonString, UserInfo.class);
        List<UserInfo> listById = userInfoRepository.findByUserId(userInfo.getUserId());
    	if (listById == null ||  (listById != null && listById.size() == 0)) {
    		return "NO_USER";
    	}
    	
    	List<UserInfo> list = userInfoRepository.findByUserIdAndPassWord(userInfo.getUserId(), userInfo.getPassWord());
    	if (list == null || (list != null && list.size() == 0)){
    		return "PASS_ERROR";
    	}
    	String userLoginId = "";
    	String tokenStr = "";
    	for(int i = 0; i < list.size(); i++) {
    		UserInfo userInfoRes = list.get(i);
    		userLoginId = userInfoRes.getUserId();
    		tokenStr = 
    				JwtUtils.createJwt(userLoginId, userInfoRes.getUserName(), BeanMap.create(userInfoRes));
    		break;
    	}
    	
    	return "UserId:" + userLoginId + ":" + tokenStr ;
    }
    
    public String updatePassWord(String userId, 
    		String newPassword) {
    	int kns = userInfoRepository.updateUserInfo(userId, newPassword);
        return String.valueOf(kns);
    }
}
