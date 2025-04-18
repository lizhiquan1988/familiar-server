package com.example.demo.repository;

import com.example.demo.model.MaintainInfo;
import com.example.demo.model.MiniProgramUserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintainInfoRepository  extends JpaRepository<MaintainInfo, Integer> {
    MaintainInfo findAllByPageId(String pageId);
}
