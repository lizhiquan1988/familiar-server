package com.example.demo.controller;

import com.example.demo.model.MedicineDataInfo;
import com.example.demo.model.SensorDataInfo;
import com.example.demo.repository.MedicineRepository;
import com.example.demo.service.MedicineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/medicine")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    private final MedicineRepository medicineRepository;

    public MedicineController(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @PostMapping("/taken")
    public String taken(@RequestBody MedicineDataInfo distanceData) {
        medicineService.recordTaken();
        MedicineDataInfo saveData = new MedicineDataInfo();
        saveData.setDistance(distanceData.getDistance());
        saveData.setTimestamp(distanceData.getTimestamp());
        medicineRepository.save(saveData);
        return "ok";
    }

    @GetMapping("/reset")
    public String reset() {
        medicineService.resetTaken();
        return "ok";
    }

    @PostMapping("/tempWarn")
    public ResponseEntity<String> receiveData(@RequestBody SensorDataInfo data) {
        medicineService.tempIsHigh(data.getTemperature());
        return ResponseEntity.ok("Send mail");
    }
}


