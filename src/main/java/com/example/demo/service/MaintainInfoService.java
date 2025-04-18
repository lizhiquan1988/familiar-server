package com.example.demo.service;

import com.example.demo.model.MaintainInfo;
import com.example.demo.repository.MaintainInfoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintainInfoService {
    private final MaintainInfoRepository maintainInfoRepository;

    public MaintainInfo findAllByPageId(String pageId) {
        return maintainInfoRepository.findAllByPageId(pageId);
    }
}
