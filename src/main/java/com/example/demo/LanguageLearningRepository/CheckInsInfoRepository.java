package com.example.demo.LanguageLearningRepository;

import com.example.demo.LanguageLearningModel.CheckInsInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInsInfoRepository  extends JpaRepository<CheckInsInfo, Integer> {
    CheckInsInfo findByOpenIdAndYearMonthValue(String openId, int yearMonthValue);
}
