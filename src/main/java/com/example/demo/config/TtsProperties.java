package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tts")
public class TtsProperties {

    private String wavFilePath;
    private String mp3FilePath;
    private String downloadUrl;

    public String getWavFilePath() {
        return wavFilePath;
    }

    public void setWavFilePath(String wavFilePath) {
        this.wavFilePath = wavFilePath;
    }

    public String getMp3FilePath() {
        return mp3FilePath;
    }

    public void setMp3FilePath(String mp3FilePath) {
        this.mp3FilePath = mp3FilePath;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
