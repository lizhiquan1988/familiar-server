package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "maintain_info")
public class MaintainInfo {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer num;

    @Column(name = "page_id", nullable = false, length = 100)
    private String pageId;

    @Column(name = "page_name", nullable = false, length = 150)
    private String pageName;

    @Column(name = "is_maintenance", nullable = false, length = 10)
    private boolean isMaintenance;

    @Column(name = "maintain_desc", nullable = false, length = 200)
    private String maintainDesc;

}

