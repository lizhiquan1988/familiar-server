package com.example.demo.websocket.netty.manager;

import com.example.demo.poker.GameRoom;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {

    private static final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();

    public static GameRoom joinRoom(String roomId, String playerId, Channel ch) {

        GameRoom room = rooms.computeIfAbsent(roomId, k -> new GameRoom(roomId));

        room.addPlayer(playerId, ch);

        return room;
    }

    public static GameRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }


}
