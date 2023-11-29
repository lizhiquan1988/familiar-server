package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.model.UserInfo;

public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
	List<UserInfo> findByUserId(String userId);
	List<UserInfo> findByUserIdAndPassWord(String userId, String passWord);
	
	@Modifying
	@Query("UPDATE UserInfo SET passWord=?2 WHERE userId=?1")
	int updateUserInfo(String userId, String password);
}
