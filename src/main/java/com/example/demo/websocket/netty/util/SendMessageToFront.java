package com.example.demo.websocket.netty.util;

import com.example.demo.websocket.netty.handler.NettyServerHandler;
import com.example.demo.websocket.netty.protocol.SendMessageDataForAiSpeaker;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class SendMessageToFront {
    public static void sendTo(SendMessageDataForAiSpeaker sendMessageDataForAiSpeaker,Logger logger) {
        Map<String, Object> response = new HashMap<>();
        try {
            NettyServerHandler.sendMessage(CoderUtil.encodeToJson(sendMessageDataForAiSpeaker));
        } catch(Exception e) {
            response.put("code", HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.put("msg", "消息发送失敗");
            logger.info(response.toString());
        }
    }
}
