package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "user_info")
public class UserInfo {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer num;
    
    @Column(name = "user_id", nullable = false, length = 10)
    private String userId;

    @Column(name = "user_name", nullable = false, length = 30)
    private String userName;
    
    @Column(name = "pass_word", nullable = false, length = 10)
    private String passWord;
    
    @Column(name = "check_pass", nullable = false, length = 10)
    private String checkPass;
    
    @Column(name = "child_grade", nullable = true, length = 10)
    private Integer childGrade;
    
    @Column(name = "mail_address", nullable = true, length = 50)
    private String mailAddress;
    
    @Column(name = "familily_id", nullable = false, length = 10)
    private String famililyId;
    
    @Column(name = "relation_ship_code", nullable = false, length = 1)
    private String relationShipCode;

    @Column(name = "relation_ship_name", nullable = false, length = 10)
    private String relationShipName;
    
    @Column(name = "update_time", updatable = false, insertable = false)
    private String updateTime;
}
