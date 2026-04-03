package com.example.demo.websocket.netty.handler;

import com.example.demo.poker.GameRoom;
import com.example.demo.utils.JapanLocalTime;
import com.example.demo.websocket.netty.manager.RoomManager;
import com.example.demo.websocket.netty.protocol.SendMessageDataForAiSpeaker;
import com.example.demo.websocket.netty.util.SendMessageToFront;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.example.demo.websocket.netty.handler.MultiPathHandler.PLAYER_ID;
import static com.example.demo.websocket.netty.handler.MultiPathHandler.ROOM_ID;

@Slf4j
@ChannelHandler.Sharable
@Component
public class PokerGameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    static final AttributeKey<Boolean> ROOM_JOINED = AttributeKey.valueOf("pokerRoomJoined");
    private static final String ROOM_FULL_MESSAGE = "The room is full";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        try {
            if (frame instanceof TextWebSocketFrame textFrame) {
                handleTextFrame(ctx, textFrame);
            } else if (frame instanceof PingWebSocketFrame) {
                ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            } else if (frame instanceof CloseWebSocketFrame) {
                ctx.channel().close();
            } else {
                log.warn("unsupported WebSocketFrame type: {}", frame.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("failed to process poker frame: {}", e.getMessage(), e);
            ctx.close();
        }
    }

    private void handleTextFrame(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String text = frame.text();
        if (text == null || text.isBlank()) {
            return;
        }

        String roomId = ctx.channel().attr(ROOM_ID).get();
        String playerId = ctx.channel().attr(PLAYER_ID).get();
        if (roomId == null || roomId.isBlank() || playerId == null || playerId.isBlank()) {
            sendError(ctx, "INVALID_ROOM", "Missing roomId or playerId");
            ctx.close();
            return;
        }

        GameRoom room = joinRoomIfNecessary(ctx, roomId, playerId);
        if (room == null) {
            return;
        }

        room.onMessage(playerId, text);
    }

    private GameRoom joinRoomIfNecessary(ChannelHandlerContext ctx, String roomId, String playerId) {
        if (Boolean.TRUE.equals(ctx.channel().attr(ROOM_JOINED).get())) {
            return RoomManager.getRoom(roomId);
        }

        GameRoom existingRoom = RoomManager.getRoom(roomId);
        if (existingRoom != null && existingRoom.isFull() && !existingRoom.hasPlayer(playerId)) {
            sendError(ctx, "ROOM_FULL", ROOM_FULL_MESSAGE);
            return null;
        }

        GameRoom room = RoomManager.joinRoom(roomId, playerId, ctx.channel());
        ctx.channel().attr(ROOM_JOINED).set(Boolean.TRUE);
        log.info("player joined room: roomId={}, playerId={}", roomId, playerId);
        return room;
    }

    private void sendError(ChannelHandlerContext ctx, String cmd, String message) {
        SendMessageDataForAiSpeaker sendMessage = new SendMessageDataForAiSpeaker(
                cmd,
                JapanLocalTime.getJapanNowTimestampSeconds(),
                message,
                0,
                "",
                ""
        );
        SendMessageToFront.sendTo(ctx, sendMessage, logger);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("poker handler exception: {}", cause.getMessage(), cause);
        ctx.close();
    }
}
