package com.example.demo.controller;

import com.example.demo.service.LinePushUserIdService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.message.TextMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class LineCallbackController {
    private static final Logger logger = LoggerFactory.getLogger(LineCallbackController.class);

    private final ObjectMapper objectMapper;
    private final LinePushUserIdService linePushUserIdService;
    private final ObjectProvider<LineMessagingClient> lineMessagingClientProvider;

    @PostMapping("/lineCallback")
    public ResponseEntity<String> messageFromLine(HttpServletRequest request) {
        String requestBody = getRequestJsonData(request);
        if (requestBody.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Failed to get user data.\"}");
        }

        logger.info("queryStr={}", requestBody);

        String userId = extractUserId(requestBody);
        if (StringUtils.hasText(userId)) {
            linePushUserIdService.updateUserId(userId);
            logger.info("LINE push userId updated: {}", userId);
            replyIfPossible(requestBody, "薬リマインダー機能に登録済です。");
        } else {
            replyIfPossible(requestBody, "薬リマインダー機能使いたい場合、「薬リマインド登録」で返信してください。");
        }

        return ResponseEntity.ok("success");
    }

    private void replyIfPossible(String requestBody, String replyMessage) {
        LineMessagingClient client = lineMessagingClientProvider.getIfAvailable();
        if (client == null) {
            logger.info("line client is null");
            return;
        }

        try {
            CallbackRequest callbackRequest = objectMapper.readValue(requestBody, CallbackRequest.class);
            for (Event event : callbackRequest.getEvents()) {
                if (event instanceof MessageEvent messageEvent) {
                    String replyToken = messageEvent.getReplyToken();
                    logger.info("lineCallback replyToken={}", replyToken);
                    client.replyMessage(new ReplyMessage(
                            replyToken,
                            new TextMessage(replyMessage)));
                }
            }
        } catch (IOException e) {
            logger.error("failed to parse LINE callback body for reply", e);
        }
    }

    private String extractUserId(String requestBody) {
        try {
            JsonNode root = objectMapper.readTree(requestBody);
            JsonNode events = root.path("events");
            if (!events.isArray() || events.isEmpty()) {
                return "";
            }
            JsonNode firstEvent = events.get(0);
            String messageText = firstEvent.path("message").path("text").asText("");
            if (!"薬リマインド登録".equals(messageText)) {
                return "";
            }
            return firstEvent.path("source").path("userId").asText("");
        } catch (IOException e) {
            logger.error("failed to parse LINE callback body", e);
            return "";
        }
    }

    private String getRequestJsonData(HttpServletRequest request) {
        try (var inputStream = request.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("failed to read LINE callback body", e);
            return "";
        }
    }
}
