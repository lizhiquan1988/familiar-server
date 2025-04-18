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
@Table(name = "location_info")
public class LocationInfo {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer num;
    
    @Column(name = "user_id", nullable = false, length = 10)
    private String userId;

    @Column(name = "latitude", nullable = false, length = 30)
    private Double latitude;
    
    @Column(name = "longitude", nullable = false, length = 30)
    private Double longitude;
    
    @Column(name = "last_flag", nullable = false, length = 1)
    private String lastFlag;
    
    @Column(name = "update_time", updatable = false, insertable = false)
    private String updateTime;
}
