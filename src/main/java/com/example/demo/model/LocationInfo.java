package com.example.demo.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "location_info")
public class LocationInfo {
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer num;
    
    @Column(name = "user_id", nullable = false, length = 10)
    @Getter
    @Setter
    private String userId;

    @Column(name = "latitude", nullable = false, length = 30)
    @Getter
    @Setter
    private Double latitude;
    
    @Column(name = "longitude", nullable = false, length = 30)
    @Getter
    @Setter
    private Double longitude;
    
    @Column(name = "last_flag", nullable = false, length = 1)
    @Getter
    @Setter
    private String lastFlag;
    
    @Column(name = "update_time", updatable = false, insertable = false)
    @Getter
    @Setter
    private String updateTime;
}
