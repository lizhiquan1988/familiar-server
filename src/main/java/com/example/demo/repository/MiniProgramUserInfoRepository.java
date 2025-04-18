package com.example.demo.repository;

import com.example.demo.model.MiniProgramUserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MiniProgramUserInfoRepository extends JpaRepository<MiniProgramUserInfo, Integer> {
    List<MiniProgramUserInfo> findByUserId(String userId);

    @Modifying
    @Query("UPDATE MiniProgramUserInfo SET nickName=?2,avatarUrl=?3 WHERE openId=?1")
    int updateNickNameAndAvatarUrl(String openId, String nickName, String avatarUrl);

    @Query("SELECT u FROM MiniProgramUserInfo u WHERE u.openId=?1")
    List<MiniProgramUserInfo> findByOpenId(String openId);
}

