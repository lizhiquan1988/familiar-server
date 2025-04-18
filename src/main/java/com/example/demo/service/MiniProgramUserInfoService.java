package com.example.demo.service;

import com.alibaba.fastjson2.JSONObject;
import com.example.demo.model.MiniProgramUserInfo;
import com.example.demo.repository.MiniProgramUserInfoRepository;
import com.example.demo.utils.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MiniProgramUserInfoService {
    private final MiniProgramUserInfoRepository miniProgramUserInfoRepository;


    public String registerUser(String jsonString) {
        MiniProgramUserInfo userInfo = JSONObject.parseObject(jsonString, MiniProgramUserInfo.class);
        List<MiniProgramUserInfo> list = miniProgramUserInfoRepository.findByOpenId(userInfo.getOpenId());
        if (list != null && !list.isEmpty()) {
            return "EXIST";
        }

        userInfo.setCreateTime(new Date());
        userInfo.setUpdateTime(new Date());
        miniProgramUserInfoRepository.save(userInfo);
        return "OK";
    }

    public MiniProgramUserInfo getUserInfo(String jsonString) {
        MiniProgramUserInfo userInfo = JSONObject.parseObject(jsonString, MiniProgramUserInfo.class);
        List<MiniProgramUserInfo> listById = miniProgramUserInfoRepository.findByOpenId(userInfo.getOpenId());
        if (listById == null || listById.isEmpty()) {
            return null;
        }

        return listById.get(0) ;
    }

    public int updateUserInfo(String jsonString) {
        MiniProgramUserInfo userInfo = JSONObject.parseObject(jsonString, MiniProgramUserInfo.class);
        return miniProgramUserInfoRepository.updateNickNameAndAvatarUrl(userInfo.getOpenId(), userInfo.getNickName(), userInfo.getAvatarUrl());
    }

    public String getUserToken(String openId, String nickName, MiniProgramUserInfo userInfoRes) {
        return JwtUtils.createJwt(openId, nickName, BeanMap.create(userInfoRes));
    }


}
