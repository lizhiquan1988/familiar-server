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
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
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
    private final Gson gson = new Gson();
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ChatHistoryService chatHistoryService;

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public NettyServerHandler(GlobalState globalState, MedicineService medicineService) {
        this.globalState = globalState;
        this.medicineService = medicineService;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        channels.add(ctx.channel());
        log.info("channel added: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        channels.remove(ctx.channel());
        log.info("channel removed: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        log.info("channel active: {}", channel.remoteAddress());
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
                log.warn("unsupported WebSocketFrame type: {}", frame.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("failed to process WebSocketFrame: {}", e.getMessage(), e);
            ctx.close();
        }
    }

    private void handleTextFrame(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String text = frame.text();
        if (text == null || text.isEmpty()) {
            return;
        }

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
                log.info("heartbeat received from channelId=[{}]", client.id());
            } else if (MsgActionEnum.LANGUAGE.getName().equals(cmd)) {
                log.info("current language: {}", globalState.getValue());
                SendMessageDataForAiSpeaker sendMessage = new SendMessageDataForAiSpeaker(
                        "CURRENT_LANGUAGE",
                        JapanLocalTime.getJapanNowTimestampSeconds(),
                        "OK",
                        0,
                        String.valueOf(globalState.getValue()),
                        "");
                SendMessageToFront.sendTo(ctx, sendMessage, logger);
            } else if (MsgActionEnum.CHANGELANGUAGE.getName().equals(cmd)) {
                log.info("change language requested: {}", languageCode);
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
                        String.valueOf(changeLanguageTo),
                        "");
                SendMessageToFront.sendTo(ctx, sendMessage, logger);
            } else if (MsgActionEnum.CLEARAIHISTORY.getName().equals(cmd)) {
                List<Message> histories = chatHistoryService.getHistory();
                chatHistoryService.clearMessage();
                SendMessageDataForAiSpeaker sendMessage = new SendMessageDataForAiSpeaker(
                        "CLEAR_AI_HISTORY_RESULT",
                        JapanLocalTime.getJapanNowTimestampSeconds(),
                        "OK",
                        histories.size(),
                        "",
                        "");
                SendMessageToFront.sendTo(ctx, sendMessage, logger);
            } else if (MsgActionEnum.RESETMEDICATIONTIME.getName().equals(cmd)) {
                medicineService.resetMedicine();
                SendMessageDataForAiSpeaker sendMessage = new SendMessageDataForAiSpeaker(
                        "RESET_MEDICATION_TIME",
                        JapanLocalTime.getJapanNowTimestampSeconds(),
                        "OK",
                        0,
                        "",
                        "");
                SendMessageToFront.sendTo(ctx, sendMessage, logger);
            } else if (MsgActionEnum.GETAIHISTORY.getName().equals(cmd)) {
                long aiChatHistory = chatHistoryService.getHistory().size();
                SendMessageDataForAiSpeaker sendMessage = new SendMessageDataForAiSpeaker(
                        "CURRENT_AI_HISTORY_AMOUNT",
                        JapanLocalTime.getJapanNowTimestampSeconds(),
                        "OK",
                        aiChatHistory,
                        "",
                        "");
                SendMessageToFront.sendTo(ctx, sendMessage, logger);
            } else if (cmd != null) {
                processor.dealMsg(ctx, text);
            } else {
                log.warn("message missing cmd field: {}", text);
            }
        } catch (JsonSyntaxException e) {
            log.warn("invalid JSON payload: {}, error: {}", text, e.getMessage());
        }
    }

    private void handleBinaryFrame(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) {
        ByteBuf byteBuf = frame.content();
        byte[] audioChunk = ByteBufUtil.getBytes(byteBuf);

        log.info("audio chunk received: {} bytes", audioChunk.length);
        processor.dealAudioChunk(ctx, audioChunk);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("handler exception: {}", cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        processor.removeChannelSendMsg(ctx.channel());
    }

    public static void sendMessage(ChannelHandlerContext ctx, String message) {
        if (ctx == null) {
            return;
        }
        ctx.channel().writeAndFlush(new TextWebSocketFrame(message));
        log.info("message sent to current channel: {}", message);
    }

    public static void sendMessage(String message) {
        if (!channels.isEmpty()) {
            channels.writeAndFlush(new TextWebSocketFrame(message));
            log.info("message broadcast to channels: {}", message);
        }
    }

    public static void sendBinary(byte[] data) {
        if (!channels.isEmpty()) {
            ByteBuf buf = Unpooled.wrappedBuffer(data);
            channels.writeAndFlush(new BinaryWebSocketFrame(buf));
            log.info("binary message broadcast to channels: {} bytes", data.length);
        }
    }
}
