package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.model.LocationInfo;

public interface LocationInfoRepository extends JpaRepository<LocationInfo, Integer> {
	List<LocationInfo> findByUserIdAndLastFlag(String userId, String lastFlag);
	
	@Modifying
	@Query("UPDATE LocationInfo SET latitude=?2,longitude=?3 WHERE userId=?1")
	int updateLocation(String userId, Double latitude, Double longtitude);
}