package com.example.demo.controller;

import com.example.demo.config.SensorCache;
import com.example.demo.model.SensorDataInfo;
import com.example.demo.repository.SensorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/sensor")
public class SensorController {

    private final SensorRepository repository;

    private final SensorCache cache = new SensorCache();

    public SensorController(SensorRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/data")
    public ResponseEntity<String> receiveData(@RequestBody SensorDataInfo data) {
        // 更新緩存
        cache.setTemperature(data.getTemperature());
        cache.setHumidity(data.getHumidity());
        cache.setTimestamp(ZonedDateTime.now(ZoneId.of("Asia/Tokyo")).toLocalDateTime());

        // 判斷是否需要寫入資料庫
        if (cache.shouldSave()) {
            SensorDataInfo saveData = new SensorDataInfo();
            saveData.setTemperature(cache.getTemperature());
            saveData.setHumidity(cache.getHumidity());
            saveData.setTimestamp(cache.getTimestamp());
            repository.save(saveData);

            cache.updateLastSaved();
            return ResponseEntity.ok("Saved to DB");
        }

        return ResponseEntity.ok("Cached, not saved");
    }

    // 实时数据（取最新一条）
    @GetMapping("/latest")
    public SensorDataInfo latest() {
        SensorDataInfo latest = new SensorDataInfo();
        latest.setTemperature(cache.getTemperature());
        latest.setHumidity(cache.getHumidity());
        latest.setTimestamp(cache.getTimestamp());
        return latest;
    }

    // 获取一天的数据（按小时分组）
    @GetMapping("/day")
    public List<SensorDataInfo> dayData(@RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime start = localDate.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        return repository.findByTimestampBetween(start, end);
    }

    @GetMapping("/list")
    public List<SensorDataInfo> list() {
        return repository.findAll();
    }
}

