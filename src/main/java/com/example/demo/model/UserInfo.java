package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_info")
public class UserInfo {
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer num;
    
    @Column(name = "user_id", nullable = false, length = 10)
    @Getter
    @Setter
    private String userId;

    @Column(name = "user_name", nullable = false, length = 30)
    @Getter
    @Setter
    private String userName;
    
    @Column(name = "pass_word", nullable = false, length = 10)
    @Getter
    @Setter
    private String passWord;
    
    @Column(name = "check_pass", nullable = false, length = 10)
    @Getter
    @Setter
    private String checkPass;
    
    @Column(name = "child_grade", nullable = true, length = 10)
    @Getter
    @Setter
    private Integer childGrade;
    
    @Column(name = "mail_address", nullable = true, length = 50)
    @Getter
    @Setter
    private String mailAddress;
    
    @Column(name = "familily_id", nullable = false, length = 10)
    @Getter
    @Setter
    private String famililyId;
    
    @Column(name = "relation_ship_code", nullable = false, length = 1)
    @Getter
    @Setter
    private Double relationShipCode;

    @Column(name = "relation_ship_name", nullable = false, length = 10)
    @Getter
    @Setter
    private String relationShipName;
    
    @Column(name = "update_time", updatable = false, insertable = false)
    @Getter
    @Setter
    private String updateTime;
}
