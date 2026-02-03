package com.example.demo.Component;

import com.example.demo.model.chatgpt.Message;
import com.example.demo.service.ChatAI.ChatGptService;
import com.example.demo.service.ChatAI.ChatHistoryService;
import com.example.demo.service.SpeechToText.AssemblyAiService;
import com.example.demo.service.TexttoSpeech.TextToSpeechService;
import com.example.demo.service.WeatherForecastService;
import com.example.demo.utils.JapanLocalTime;
import com.example.demo.websocket.netty.handler.NettyServerHandler;
import com.example.demo.websocket.netty.protocol.SendMessageDataForAiSpeaker;

import com.example.demo.websocket.netty.util.SendMessageToFront;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TranscriptionTaskManager {

    private final AssemblyAiService assemblyAiService;

    @Autowired
    private ChatGptService chatGptService;

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Autowired
    private TextToSpeechService textToSpeechService;

    @Autowired
    private WeatherForecastService weatherForecastService;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ExecutorService executor = Executors.newCachedThreadPool();

    // 存放 transcriptId -> 状态/结果
    private final Map<String, Map<String, Object>> taskResults = new ConcurrentHashMap<>();

    public TranscriptionTaskManager(AssemblyAiService assemblyAiService) {
        this.assemblyAiService = assemblyAiService;
    }

    // 添加任务
    public void addTask(String transcriptId) {
        taskResults.put(transcriptId, Map.of("status", "queued"));
    }

    // 定时轮询 AssemblyAI，检查任务结果
    @Scheduled(fixedDelay = 500) // 每0.5秒执行一次
    public void checkTasks() {
        for (String transcriptId : taskResults.keySet()) {
            Map<String, Object> result = assemblyAiService.getTranscriptionResult(transcriptId);

            if (result != null) {
                String status = (String) result.get("status");
                taskResults.put(transcriptId, result);
                // 如果已经完成，就可以选择移除，或者继续保留
                if ("completed".equals(status)) {
                    askGpt(transcriptId, (String) result.get("text"));
                } else if ("error".equals(status)) {
                    System.out.println("转写任务失敗: " + transcriptId);
                }
            }
        }
    }

    private void askGpt(String transcriptId, String question) {
        logger.info("语音识别结果：{}",question);
        String weatherInfo = addWeatherForecastQuestion(question);
        question = weatherInfo.isEmpty()? question : weatherInfo;
        if (question.equals("playMusic")) {
            SendMessageDataForAiSpeaker sendMessage = new SendMessageDataForAiSpeaker(
                    "PLAY_MUSIC",
                    JapanLocalTime.getJapanNowTimestampSeconds(),
                    "you can over Websocket.",
                    0,
                    "",
                    "");
            SendMessageToFront.sendTo(sendMessage, logger);
            taskResults.remove(transcriptId);
            return;
        }
        chatHistoryService.addMessage(new Message("user", question));
        String answer = chatGptService.askGpt(chatHistoryService.getHistory());
        chatHistoryService.addMessage(new Message("assistant", answer));

        logger.info("ChatAi文字回答结果：{}", answer);

        String ttsAudioUrl = textToSpeechService.speak(answer);
        // 发二进制音频帧（前端收到后播放）
//        NettyServerHandler.sendBinary(ttsAudio);

        // 发送语音文件链接给前端
        SendMessageDataForAiSpeaker sendMessage = new SendMessageDataForAiSpeaker(
                "PLAY_AUDIO",
                JapanLocalTime.getJapanNowTimestampSeconds(),
                "you can over Websocket.",
                0,
                "",
                ttsAudioUrl);
        SendMessageToFront.sendTo(sendMessage, logger);

        SendMessageDataForAiSpeaker sendOverMessage = new SendMessageDataForAiSpeaker(
                "GPT_RESPONSE",
                JapanLocalTime.getJapanNowTimestampSeconds(),
                "you can over Websocket.",
                0,
                answer,"");
        SendMessageToFront.sendTo(sendOverMessage, logger);

        taskResults.remove(transcriptId);
    }

    private String addWeatherForecastQuestion(String question) {
        if (question.contains("天気") || question.contains("天气") || question.contains("天氣")) {
            Map<String, Object> weatherResult = weatherForecastService.getOpenWeatherForecast();

            String prompt = null; // 用于传给 ChatGPT 的内容
            String dayLabel = "";

            // 关键词映射到对应的 JSON 数据
            Map<List<String>, Object> keywordMap = Map.of(
                    List.of("今天", "今日"), weatherResult.get("today"),
                    List.of("現在", "实时"), weatherResult.get("current")
            );

            // 先匹配今天或实时
            boolean matched = false;
            for (Map.Entry<List<String>, Object> entry : keywordMap.entrySet()) {
                for (String keyword : entry.getKey()) {
                    if (question.contains(keyword)) {
                        prompt = entry.getValue().toString();
                        dayLabel = keyword;
                        matched = true;
                        break;
                    }
                }
                if (matched) break;
            }

            // 如果没有匹配到今天或实时，则匹配未来
            if (!matched) {
                @SuppressWarnings("unchecked")
                List<String> futureDays = (List<String>) weatherResult.get("future6days"); // 未来6天
                Map<List<String>, Integer> futureKeywordMap = Map.of(
                        List.of("明天", "明日", "あした"), 0,
                        List.of("后天", "後天","明後日", "あさて", "あさって"), 1,
                        List.of("大后天", "三日間後", "みかかんご"), 2
                );

                for (Map.Entry<List<String>, Integer> entry : futureKeywordMap.entrySet()) {
                    for (String keyword : entry.getKey()) {
                        if (question.contains(keyword) && entry.getValue() < futureDays.size()) {
                            prompt = futureDays.get(entry.getValue());
                            dayLabel = keyword;
                            matched = true;
                            break;
                        }
                    }
                    if (matched) break;
                }
            }

            // 如果匹配到对应 JSON，则追加一句话总结指令
            if (prompt != null) {
                return dayLabel + "天气：" + prompt + "请根据以上内容用一句话简单总结天气情况";
            }
            // 如果没有匹配到默认播报今天的天气
            else {
                // 关键词映射到对应的 JSON 数据
                Map<List<String>, Object> defaultKeywordMap = Map.of(
                        List.of("今天", "今日"), weatherResult.get("today")
                );

                // 匹配今天
                boolean defaultMatched = false;
                for (Map.Entry<List<String>, Object> entry : defaultKeywordMap.entrySet()) {
                    for (String keyword : entry.getKey()) {
                        if (question.contains(keyword)) {
                            prompt = entry.getValue().toString();
                            dayLabel = keyword;
                            defaultMatched = true;
                            break;
                        }
                    }
                    if (defaultMatched) break;
                }
                return dayLabel + "天气：" + prompt + "请根据以上内容用一句话简单总结天气情况";
            }
        } else if (question.contains("播放音乐") ||
                question.contains("音楽流して") ||
                question.contains("おんがくながして") ||
                question.contains("播放音樂")) {
            return "playMusic";
        } else {
            return "";
        }
    }
}
