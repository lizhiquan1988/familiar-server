package com.example.demo.websocket.netty.handler;

import com.example.demo.Component.GlobalState;
import com.example.demo.model.chatgpt.Message;
import com.example.demo.poker.GameRoom;
import com.example.demo.service.ChatAI.ChatHistoryService;
import com.example.demo.service.MedicineService;
import com.example.demo.utils.JapanLocalTime;
import com.example.demo.websocket.netty.manager.RoomManager;
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
public class PokerGameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final MsgProcessor processor = new MsgProcessor();
    private static ChannelHandlerContext channelHandlerContext;
    private final Gson gson = new Gson();
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ChatHistoryService chatHistoryService;

    // 存储所有连接的客户端 Channel
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

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
                handleTextFrameForPokerGame(ctx, (TextWebSocketFrame) frame);
            } else if (frame instanceof BinaryWebSocketFrame) {
//                handleBinaryFrame(ctx, (BinaryWebSocketFrame) frame);
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

    private void handleTextFrameForPokerGame(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String text = frame.text();
        if (text == null || text.isEmpty()) return;
        SendMessageDataForAiSpeaker sendMessage = new SendMessageDataForAiSpeaker(
                    "ROOM_FULL",
                    JapanLocalTime.getJapanNowTimestampSeconds(),
                    "The room is full",
                    0,
                    "房间已满","");
            SendMessageToFront.sendTo(sendMessage, logger);

//        String roomId = ctx.channel().attr(WsPathHandler.ROOM_ID).get();
//        String playerId = ctx.channel().attr(WsPathHandler.PLAYER_ID).get();
//
//        GameRoom room = RoomManager.joinRoom(roomId, playerId, ctx.channel());
//        if (!room.isStarted()) {
//            RoomManager.joinRoom(roomId, playerId, ctx.channel());
//        } else {
//            SendMessageDataForAiSpeaker sendMessage = new SendMessageDataForAiSpeaker(
//                    "ROOM_FULL",
//                    JapanLocalTime.getJapanNowTimestampSeconds(),
//                    "The room is full",
//                    0,
//                    "房间已满","");
//            SendMessageToFront.sendTo(sendMessage, logger);
//        }


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
}
