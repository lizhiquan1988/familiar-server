package com.example.demo.LanguageLearningRepository;

import com.example.demo.LanguageLearningModel.LanguageLearningUserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageLearningUserInfoRepository extends JpaRepository<LanguageLearningUserInfo, Integer> {
    LanguageLearningUserInfo findLanguageLeaningUserInfoByOpenId(String openId);
}
