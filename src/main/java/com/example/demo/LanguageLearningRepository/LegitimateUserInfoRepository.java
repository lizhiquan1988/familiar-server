package com.example.demo.LanguageLearningRepository;

import com.example.demo.LanguageLearningModel.LegitimateUserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LegitimateUserInfoRepository extends JpaRepository<LegitimateUserInfo, Integer> {
    @Query("SELECT u FROM LegitimateUserInfo u WHERE u.studentGrade=?1 and u.studentName=?2 ORDER BY u._id")
    LegitimateUserInfo findByStudentGradeAndStudentName(int grade, String name);
}
