package com.example.demo.LanguageLearningService;

import com.example.demo.LanguageLearningModel.LanguageLearningUserInfo;
import com.example.demo.LanguageLearningRepository.LanguageLearningUserInfoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class LanguageLearningUserInfoService {
    private final LanguageLearningUserInfoRepository languageLearningUserInfoRepository;

    public LanguageLearningUserInfo getLanguageLearningUserInfo(String openId) {
        return languageLearningUserInfoRepository.findLanguageLeaningUserInfoByOpenId(openId);
    }


    public void updateLanguageLearningUserInfo(LanguageLearningUserInfo languageLearningUserInfo) {
        LanguageLearningUserInfo oldLanguageLearningUserInfo =
                languageLearningUserInfoRepository.findLanguageLeaningUserInfoByOpenId(languageLearningUserInfo.getOpenId());
        oldLanguageLearningUserInfo.setAvatarUrl(languageLearningUserInfo.getAvatarUrl());
        oldLanguageLearningUserInfo.setIsAdmin(languageLearningUserInfo.getIsAdmin());
        oldLanguageLearningUserInfo.setLastStudyDate(languageLearningUserInfo.getLastStudyDate());
        oldLanguageLearningUserInfo.setUserLevelDesc(languageLearningUserInfo.getUserLevelDesc());
        oldLanguageLearningUserInfo.setNickName(languageLearningUserInfo.getNickName());
        oldLanguageLearningUserInfo.setStudentGrade(languageLearningUserInfo.getStudentGrade());
        oldLanguageLearningUserInfo.setStudyDays(languageLearningUserInfo.getStudyDays());
        oldLanguageLearningUserInfo.setUpdateTime(LocalDateTime.now());
        languageLearningUserInfoRepository.save(languageLearningUserInfo);
    }

    public void saveLanguageLearningUserInfo(LanguageLearningUserInfo languageLearningUserInfo) {
        languageLearningUserInfo.setCreateTime(LocalDateTime.now());
        languageLearningUserInfo.setUpdateTime(LocalDateTime.now());
        languageLearningUserInfoRepository.save(languageLearningUserInfo);
    }
}
