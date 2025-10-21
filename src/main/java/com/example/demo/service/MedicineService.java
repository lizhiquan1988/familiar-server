package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MedicineService {

    @Autowired
    private EmailService emailService;

    private boolean takenToday = false;

    public void recordTaken() {
        takenToday = true;
        emailService.sendEmail("478028921@qq.com", "吃药提醒", "今天已吃药 ✅");
    }

    public void resetTaken() {
        takenToday = false;
    }

    // 每天 20:00 执行一次检查
    @Scheduled(cron = "0 0 20 * * ?")
    public void checkMedicine() {
        if (!takenToday) {
            emailService.sendEmail("478028921@qq.com", "吃药提醒", "今天还没吃药，请记得服药 ❗");
        }
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void resetMedicine() {
        // 重置状态
        takenToday = false;
    }

    public void tempIsHigh(Double temp) {
        emailService.sendEmail("478028921@qq.com", "吃药設備溫度提醒", "高溫注意 ❗ ❗ ❗溫度是：" + temp);
    }
}

