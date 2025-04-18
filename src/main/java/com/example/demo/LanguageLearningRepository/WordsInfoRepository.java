package com.example.demo.LanguageLearningRepository;

import com.example.demo.LanguageLearningModel.WordsInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WordsInfoRepository  extends JpaRepository<WordsInfo, Integer> {
    @Query("SELECT u FROM WordsInfo u WHERE u.searchDate=?1 and u.category=?2 ORDER BY u.searchDate, u.category")
    List<WordsInfo> findAllBySearchDateAndCategory(String searchDate, int category);

    @Query("SELECT u FROM WordsInfo u WHERE u.searchDate=?1 ORDER BY u.searchDate")
    List<WordsInfo> findAllBySearchDate(String searchDate);
}
