package com.example.demo.websocket.netty.handler;

import com.example.demo.Component.GlobalState;
import com.example.demo.model.chatgpt.Message;
import com.example.demo.service.ChatAI.ChatHistoryService;
import com.example.demo.service.MedicineService;
import com.example.demo.utils.JapanLocalTime;
import com.example.demo.websocket.netty.processor.MsgProcessor;
import com.example.demo.websocket.netty.protocol.MsgActionEnum;
import com.example.demo.websocket.netty.protocol.SendMessageDataForAiSpeaker;
import com.example.demo.websocket.netty.util.SendMessageToFront;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@ChannelHandler.Sharable
@Component
public class NettyServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final GlobalState globalState;
    private final MedicineService medicineService;

    private final MsgProcessor processor = new MsgProcessor();
    private static ChannelHandlerContext channelHandlerContext;
    private final Gson gson = new Gson();
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ChatHistoryService chatHistoryService;

    // 存储所有连接的客户端 Channel
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public NettyServerHandler(GlobalState globalState, MedicineService medicineService) {
        this.globalState = globalState;
        this.medicineService = medicineService;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        channels.add(ctx.channel());
        log.info("新客户端 {} 添加到群: ", ctx.channel().remoteAddress());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        channels.remove(ctx.channel());
        log.info("客户端 {} 从群中删除", ctx.channel().remoteAddress());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        channelHandlerContext = ctx;
        log.info("客户端 {} 上线", channel.remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        try {
            if (frame instanceof TextWebSocketFrame) {
                handleTextFrame(ctx, (TextWebSocketFrame) frame);
            } else if (frame instanceof BinaryWebSocketFrame) {
                handleBinaryFrame(ctx, (BinaryWebSocketFrame) frame);
            } else if (frame instanceof PingWebSocketFrame) {
                ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            } else if (frame instanceof CloseWebSocketFrame) {
                ctx.channel().close();
            } else {
                log.warn("未知类型的 WebSocketFrame: {}", frame.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("WebSocketFrame 处理异常: {}", e.getMessage(), e);
            ctx.close();
        }
    }

    private void handleTextFrame(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String text = frame.text();
        if (text == null || text.isEmpty()) return;

        try {
            JsonObject json = gson.fromJson(text, JsonObject.class);
            String cmd = json.has("cmd") && !json.get("cmd").isJsonNull() ? json.get("cmd").getAsString() : null;
            String languageCode = json.has("langcode") && !json.get("langcode").isJsonNull() ? json.get("langcode").getAsString() : null;
            if (MsgActionEnum.FINISH.getName().equals(cmd)) {
                processor.finishRecording(ctx);
            } else if (MsgActionEnum.AUDIO_CANCEL.getName().equals(cmd)) {
                processor.cancelRecording(ctx);
            } else if (MsgActionEnum.HEARTBEAT.getName().equals(cmd)) {
                Channel client = ctx.channel();
                log.info("收到来自channelId为[" + client.id() + "]的心跳包...");
            } else if (cmd.equals(MsgActionEnum.LANGUAGE.getName())) {
                log.info("现在的语言是：" + globalState.getValue());
                SendMessageDataForAiSpeaker sendMessage = new SendMessageDataForAiSpeaker(
                        "CURRENT_LANGUAGE",
                        JapanLocalTime.getJapanNowTimestampSeconds(),
                        "OK",
                        0,
                        String.valueOf(globalState.getValue()),"");
                SendMessageToFront.sendTo(sendMessage, logger);
            } else if (cmd.equals(MsgActionEnum.CHANGELANGUAGE.getName())) {
                log.info("更改语言" + languageCode);
                int changeLanguageTo = 0;
                if (languageCode != null) {
                    changeLanguageTo = Integer.parseInt(languageCode);
                }
                globalState.setValue(changeLanguageTo);
                SendMessageDataForAiSpeaker sendMessage = new SendMessageDataForAiSpeaker(
                        "CHANGE_LANGUAGE_RESULT",
                        JapanLocalTime.getJapanNowTimestampSeconds(),
                        "OK",
                        0,
                        String.valueOf(changeLanguageTo), "");
                SendMessageToFront.sendTo(sendMessage, logger);
            } else if (cmd.equals(MsgActionEnum.CLEARAIHISTORY.getName())) {
                List<Message> histories = chatHistoryService.getHistory();
                chatHistoryService.clearMessage();
                SendMessageDataForAiSpeaker sendMessage = new SendMessageDataForAiSpeaker(
                        "CLEAR_AI_HISTORY_RESULT",
                        JapanLocalTime.getJapanNowTimestampSeconds(),
                        "OK",
                        histories.size(),
                        "","");
                SendMessageToFront.sendTo(sendMessage, logger);
            } else if (cmd.equals(MsgActionEnum.RESETMEDICATIONTIME.getName())) {
                medicineService.resetMedicine();
                SendMessageDataForAiSpeaker sendMessage = new SendMessageDataForAiSpeaker(
                        "RESET_MEDICATION_TIME",
                        JapanLocalTime.getJapanNowTimestampSeconds(),
                        "OK",
                        0,
                        "","");
                SendMessageToFront.sendTo(sendMessage, logger);
            } else if (cmd.equals(MsgActionEnum.GETAIHISTORY.getName())) {
                long aiChatHistory = chatHistoryService.getHistory().size();
                SendMessageDataForAiSpeaker sendMessage = new SendMessageDataForAiSpeaker(
                        "CURRENT_AI_HISTORY_AMOUNT",
                        JapanLocalTime.getJapanNowTimestampSeconds(),
                        "OK",
                        aiChatHistory,
                        "","");
                SendMessageToFront.sendTo(sendMessage, logger);
            } else if (cmd != null) {
                processor.dealMsg(ctx, text);
            } else {
                log.warn("收到无效命令: {}", text);
            }
        } catch (JsonSyntaxException e) {
            log.warn("无法解析 JSON: {}, 异常: {}", text, e.getMessage());
        }
    }

    private void handleBinaryFrame(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) {
        ByteBuf byteBuf = frame.content();
        byte[] audioChunk = ByteBufUtil.getBytes(byteBuf);

        log.info("收到音频二进制块，大小={}字节", audioChunk.length);
        processor.dealAudioChunk(ctx, audioChunk);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("连接异常：{}", cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        processor.removeChannelSendMsg(ctx.channel());
    }

    // 服务器向客户端发送文本消息
    public static void sendMessage(String message) {
        if (channelHandlerContext != null) {
            Channel client = channelHandlerContext.channel();
            client.writeAndFlush(new TextWebSocketFrame(message));
            log.info("服务器→客户端：{}", message);
        }
        // 发送给所有设备
//        if (!channels.isEmpty()) {
//            channels.writeAndFlush(new TextWebSocketFrame(message));
//            log.info("服务器→客户端：{}", message);
//        }
    }

    // 服务器向客户端发送二进制数据
    public static void sendBinary(byte[] data) {
        if (channelHandlerContext != null) {
            Channel client = channelHandlerContext.channel();
            ByteBuf buf = Unpooled.wrappedBuffer(data);
            client.writeAndFlush(new BinaryWebSocketFrame(buf));
            log.info("服务器→客户端（二进制）发送 {} 字节", data.length);
        }
    }
}
