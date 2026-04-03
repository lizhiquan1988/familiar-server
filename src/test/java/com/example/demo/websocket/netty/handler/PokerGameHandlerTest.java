package com.example.demo.websocket.netty.handler;

import com.example.demo.websocket.netty.manager.RoomManager;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PokerGameHandlerTest {

    @AfterEach
    void tearDown() {
        RoomManager.clear();
    }

    @Test
    void channelRead0_shouldRejectMissingRoomAttributes() {
        EmbeddedChannel channel = new EmbeddedChannel(new PokerGameHandler());

        channel.writeInbound(new TextWebSocketFrame("pass"));

        TextWebSocketFrame outbound = channel.readOutbound();
        assertNotNull(outbound);
        assertFalse(channel.isOpen());
        assertTrue(outbound.text().contains("\"cmd\":\"INVALID_ROOM\""));
        outbound.release();
    }

    @Test
    void channelRead0_shouldJoinRoomOnlyOnceForSamePlayer() {
        EmbeddedChannel channel = new EmbeddedChannel(new PokerGameHandler());
        channel.attr(MultiPathHandler.ROOM_ID).set("room-1");
        channel.attr(MultiPathHandler.PLAYER_ID).set("player-1");

        channel.writeInbound(new TextWebSocketFrame("pass"));
        channel.writeInbound(new TextWebSocketFrame("pass"));

        assertNotNull(RoomManager.getRoom("room-1"));
        assertTrue(RoomManager.getRoom("room-1").hasPlayer("player-1"));
    }

    @Test
    void channelRead0_shouldReplyToPingFrame() {
        EmbeddedChannel channel = new EmbeddedChannel(new PokerGameHandler());

        channel.writeInbound(new io.netty.handler.codec.http.websocketx.PingWebSocketFrame());

        PongWebSocketFrame outbound = channel.readOutbound();
        assertNotNull(outbound);
        outbound.release();
    }
}
