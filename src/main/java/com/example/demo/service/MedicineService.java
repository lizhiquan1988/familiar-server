package com.example.demo.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MedicineService {

    private static final String MAIL_TO = "478028921@qq.com";

    private final EmailService emailService;
    private final LinePushService linePushService;
    private final LinePushUserIdService linePushUserIdService;

    private boolean takenToday = false;

    public MedicineService(
            EmailService emailService,
            LinePushService linePushService,
            LinePushUserIdService linePushUserIdService) {
        this.emailService = emailService;
        this.linePushService = linePushService;
        this.linePushUserIdService = linePushUserIdService;
    }

    public void recordTaken() {
        if (takenToday) {
            return;
        }
        notifyMedicine("吃药提醒", "今天已吃药 ✅");
        takenToday = true;
    }

    public void resetTaken() {
        takenToday = false;
    }

    @Scheduled(cron = "0 0 20 * * ?")
    public void checkMedicine() {
        if (!takenToday) {
            notifyMedicine("吃药提醒", "今天还没吃药，请记得服药 ❗");
        }
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void resetMedicine() {
        takenToday = false;
    }

    public void tempIsHigh(Double temp) {
        notifyMedicine("吃药設備温度提醒", "高温注意 ❗ ❗ ❗ 温度是：" + temp);
    }

    private void notifyMedicine(String subject, String message) {
//        emailService.sendEmail(MAIL_TO, subject, message);
        String linePushUserId = linePushUserIdService.getUserId();
        if (StringUtils.hasText(linePushUserId)) {
            linePushService.pushToUser(linePushUserId, message);
        }
    }
}
