package com.example.demo.repository;

import com.example.demo.model.MedicineDataInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicineRepository extends JpaRepository<MedicineDataInfo, Long> {
    List<MedicineDataInfo> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
