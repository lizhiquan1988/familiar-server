package com.example.demo.LanguageLearningRepository;

import com.example.demo.LanguageLearningModel.RecordsInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecordsInfoRepository  extends JpaRepository<RecordsInfo, Integer> {
    @Query("SELECT u FROM RecordsInfo u WHERE u.openId=?1")
    List<RecordsInfo> findAllByOpenId(String openId);

    @Query("SELECT u FROM RecordsInfo u WHERE u.wordId=?1 ORDER BY u.score")
    List<RecordsInfo> findAllByWordId(String wordId);

    @Query("SELECT u FROM WordsInfo u WHERE u.searchDate=?1 and u.category=?2 ORDER BY u.searchDate, u.category")
    List<RecordsInfo> findAllBySearchDateAndCategory(String searchDate, int category);

    @Query("SELECT u FROM WordsInfo u WHERE u.searchDate=?1 ORDER BY u.searchDate")
    List<RecordsInfo> findAllBySearchDate(String searchDate);
}
