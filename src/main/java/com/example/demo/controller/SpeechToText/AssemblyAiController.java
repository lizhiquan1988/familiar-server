package com.example.demo.controller.SpeechToText;

import com.example.demo.Component.TranscriptionTaskManager;
import com.example.demo.service.SpeechToText.AssemblyAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@RestController
@RequestMapping("/assemblyai")
public class AssemblyAiController {

    @Autowired
    private AssemblyAiService assemblyAiService;

    @Autowired
    private TranscriptionTaskManager taskManager;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostMapping("/submit")
    public Map<String, Object> transcribe(@RequestBody byte[] audioData) {
        // Step 1: 上传音频
        String audioUrl = assemblyAiService.uploadAudio(audioData);

        logger.info("上传音频的URL："+ audioUrl);

        // Step 2: 请求转写
        String transcriptId = assemblyAiService.requestTranscription(audioUrl);

        taskManager.addTask(transcriptId);
        return Map.of("taskId", transcriptId, "status", "submitted");
    }
}
