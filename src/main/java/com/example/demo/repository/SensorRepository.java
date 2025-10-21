package com.example.demo.repository;

import com.example.demo.model.SensorDataInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SensorRepository extends JpaRepository<SensorDataInfo, Long> {
    SensorDataInfo findTopByOrderByTimestampDesc();

    List<SensorDataInfo> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}

