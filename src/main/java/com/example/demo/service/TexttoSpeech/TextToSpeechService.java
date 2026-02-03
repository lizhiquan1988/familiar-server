package com.example.demo.service.TexttoSpeech;

import com.example.demo.Component.GlobalState;
import com.example.demo.config.TtsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import com.example.demo.utils.JapanLocalTime;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class TextToSpeechService {
    private final String VIOCEVOX_BASE_URL = "http://localhost:50021";

    private final RestTemplate restTemplate = new RestTemplate();

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GlobalState globalState;

    private final TtsProperties ttsProperties;

    public TextToSpeechService(GlobalState globalState, TtsProperties ttsProperties) {
        this.globalState = globalState;
        this.ttsProperties = ttsProperties;
    }

    public String speak(String text) {
//        if (globalState.getValue() == 0) {
//            return new byte[0];
//        } else {
//            return voiceVoxSpeak(text);
//        }
        return voiceVoxSpeak(text);
    }

    /**
     * 生成指定长度的静音数据 (16-bit PCM)
     */
    private byte[] generateSilence(int sampleRate, double durationSeconds) {
        int numSamples = (int) (sampleRate * durationSeconds);
        ByteBuffer buffer = ByteBuffer.allocate(numSamples * 2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < numSamples; i++) {
            buffer.putShort((short) 0);
        }
        return buffer.array();
    }


    public String voiceVoxSpeak(String text) {
        try {
            String[] sentences = text.split("(?<=[。！？])");
            // 累积音频数据
            ByteArrayOutputStream mergedAudio = new ByteArrayOutputStream();
            // 0.5秒静音
            byte[] silence = generateSilence(24000, 0.5);
            for (int i = 0; i < sentences.length; i++) {
                String sentence = sentences[i].trim();
                // 1. 请求 VOICEVOX engine audio_query
                if (sentence.trim().isEmpty()) continue;
                String queryUrl = VIOCEVOX_BASE_URL + "/audio_query?text=" + sentence + "&speaker=2";
                String audioQuery = restTemplate.postForObject(queryUrl, null, String.class);
                logger.info("开始文字转语音处理");
                // 2. 请求 synthesis
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(audioQuery, headers);
                String synthUrl = "http://localhost:50021/synthesis?speaker=1";
                byte[] audioData = restTemplate.postForObject(synthUrl, entity, byte[].class);
                if (audioData != null && audioData.length > 44) {
                    boolean hasWavHeader = audioData[0] == 'R' && audioData[1] == 'I' && audioData[2] == 'F' && audioData[3] == 'F';
                    logger.info("是否包含WAV头: {}", hasWavHeader);
                    if (i == 0) {
                        // 第一段保留 WAV 文件头
                        mergedAudio.write(audioData);
                    } else {
                        // 其余片段去掉前44字节头部再拼接
                        mergedAudio.write(audioData, 44, audioData.length - 44);
                    }
                    mergedAudio.write(silence);
                }
            }
            if (mergedAudio.size() == 0) {
                logger.error("没有音频返回");
                return "";
            }
            // ✅ 修正 WAV 文件头长度
            byte[] finalWav = mergedAudio.toByteArray();
            fixWavHeader(finalWav);

            // 3. 保存为文件
            String filePath = ttsProperties.getWavFilePath();
            if (mergedAudio.size() > 0) {
                Files.write(Paths.get(filePath), finalWav);
                logger.info("音频文件保存成功");
                // 输出目录（存放 MP3 文件的文件夹）
                String outputDir = Paths.get(ttsProperties.getMp3FilePath()).toString();

                // 调用转换函数，返回唯一 MP3 文件路径
                String mp3Path = wavToMp3(filePath, outputDir);

                // 拼接可下载 URL（假设下载目录是 /images/audio/tts/）
                String fileName = Paths.get(mp3Path).getFileName().toString();
                String downloadUrl = ttsProperties.getDownloadUrl() + "/" + fileName;

                logger.info("语音生成完成，可下载地址：{}", downloadUrl);
                return downloadUrl;
            } else {
                logger.info("音频文件生产失败");
                return null;
            }
        } catch (Exception exception) {
            logger.error("日语语音生成时报错：${}", exception.getMessage(), exception);
            return null;
        }
    }

    /**
     * 修正拼接后 WAV 头部长度字段
     */
    private void fixWavHeader(byte[] wavData) {
        if (wavData.length < 44) return;

        int totalSize = wavData.length - 8;
        int dataSize = wavData.length - 44;

        ByteBuffer header = ByteBuffer.wrap(wavData);
        header.order(ByteOrder.LITTLE_ENDIAN);
        header.putInt(4, totalSize);   // ChunkSize
        header.putInt(40, dataSize);   // Subchunk2Size
    }

    private static void wavToPcmWav(String sourcePath, String targetPath)
            throws IOException, InterruptedException {

        File src = new File(sourcePath);
        if (!src.exists() || src.length() == 0) {
            throw new RuntimeException("源 WAV 文件不存在或为空: " + sourcePath);
        }

        List<String> command = List.of(
                "ffmpeg", "-y",
                "-i", src.getAbsolutePath(),
                "-ar", "16000",
                "-ac", "1",
                "-f", "wav",                 // 输出 WAV
                "-acodec", "pcm_s16le",      // PCM 16-bit little-endian
                targetPath
        );

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("[FFMPEG] " + line);
            }
        }

        int code = process.waitFor();
        if (code != 0) {
            throw new RuntimeException("FFmpeg 转码失败，exitCode=" + code);
        }
    }

    private static String wavToMp3(String sourcePath, String outputDir)
            throws IOException, InterruptedException {

        File src = new File(sourcePath);
        if (!src.exists() || src.length() == 0) {
            throw new RuntimeException("源 WAV 文件不存在或为空: " + sourcePath);
        }

        // 生成唯一文件名（时间戳 + UUID）
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").
                format(new Date(JapanLocalTime.getJapanNowTimestampSeconds()));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String targetFileName = "tts_" + timestamp + "_" + uniqueId + ".mp3";
        String targetPath = Paths.get(outputDir, targetFileName).toString();

        // 构造 ffmpeg 命令
        List<String> command = List.of(
                "ffmpeg", "-y",
                "-i", src.getAbsolutePath(),
                "-vn",
                "-ar", "16000",
                "-ac", "1",
                "-b:a", "64k",
                "-filter:a", "volume=3.0",
                "-codec:a", "libmp3lame",
                "-f", "mp3",
                targetPath
        );

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("[FFMPEG] " + line);
            }
        }

        int code = process.waitFor();
        if (code != 0) {
            throw new RuntimeException("FFmpeg 转码失败，exitCode=" + code);
        }

        // 等待文件写入完成
        File mp3 = new File(targetPath);
        int retries = 0;
        while ((!mp3.exists() || mp3.length() == 0) && retries < 10) {
            Thread.sleep(200);
            retries++;
        }

        if (!mp3.exists() || mp3.length() == 0) {
            throw new RuntimeException("MP3 文件未生成成功: " + targetPath);
        }

        System.out.println("[FFMPEG] MP3 文件生成成功: " + targetPath);
        return targetPath; // 返回实际生成的文件路径
    }

}
