package com.example.demo.poker;

import com.google.gson.Gson;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameRoom {
    private static final Gson GSON = new Gson();

    private final String roomId;

    // 玩家
    private final List<String> players = new ArrayList<>();
    private final Map<String, Channel> channels = new ConcurrentHashMap<>();

    // 手牌
    private final Map<String, List<String>> hands = new ConcurrentHashMap<>();

    // 已出手牌
    private final Map<String, List<String>> usedHands = new ConcurrentHashMap<>();

    // 当前出牌
    private List<String> lastCards = new ArrayList<>();

    // 当前轮到谁
    private int turnIndex = 0;

    // 记录连续pass
    private int passCount = 0;

    // 当前轮是否刚开始
    private boolean newRound = true;

    // 是否开始
    @Getter
    private boolean started = false;

    // 身份
    private String emperor;   // 皇帝
    private String guard;     // 侍卫

    // 标牌
    private final static String emperorCardPrefix = "BJ_";
    private final static String guardCardPrefix = "SJ_";

    // 阵营
    private Set<String> emperorTeam = new HashSet<>();
    private Set<String> civilianTeam = new HashSet<>();

    // 出完顺序
    private final List<String> rank = new ArrayList<>();

    private boolean gameOver = false;

    public GameRoom(String roomId) {
        this.roomId = roomId;
    }

    // ================== 玩家加入 ==================
    public synchronized void addPlayer(String playerId, Channel ch) {
        if (playerId == null || playerId.isBlank() || ch == null) {
            return;
        }

        if (channels.containsKey(playerId)) {
            channels.put(playerId, ch);
            return;
        }

        if (players.size() >= 5) {
            send(ch, "房间已满");
            return;
        }

        players.add(playerId);
        channels.put(playerId, ch);

        broadcast("玩家加入：" + playerId);

        if (players.size() == 5) {
            startGame();
        }
    }

    public synchronized boolean hasPlayer(String playerId) {
        return channels.containsKey(playerId);
    }

    public synchronized boolean isFull() {
        return players.size() >= 5;
    }

    // ================== 开始游戏 ==================
    private void startGame() {
        started = true;

        List<String> deck = generateDeck();
        Collections.shuffle(deck);

        // 初始化手牌
        for (String p : players) {
            hands.put(p, new ArrayList<>());
            usedHands.put(p, new ArrayList<>());
        }

        // 发牌
        for (int i = 0; i < deck.size(); i++) {
            hands.get(players.get(i % 5)).add(deck.get(i));
        }

        // ================= 补发3 =================
        for (String p : players) {

            String suit = getRandomSuit();

            String card = suit + "_3_0";

            hands.get(p).add(card);
        }

        // 通知每个玩家
        for (String p : players) {
            send(p, buildStartMsg(p));
        }

        // 找皇帝 & 侍卫
        emperor = players.stream()
                .filter(p -> containsCardWithPrefix(hands.get(p), emperorCardPrefix))
                .findFirst()
                .orElse(players.get(0));

        guard = players.stream()
                .filter(p -> !p.equals(emperor))
                .filter(p -> containsCardWithPrefix(hands.get(p), guardCardPrefix))
                .findFirst()
                .orElseGet(() -> players.stream()
                        .filter(p -> !p.equals(emperor))
                        .findFirst()
                        .orElse(emperor));

        // 阵营划分
        emperorTeam.add(emperor);
        emperorTeam.add(guard);

        for (String p : players) {
            if (!emperorTeam.contains(p)) {
                civilianTeam.add(p);
            }
        }

        // 设置皇帝先出
        turnIndex = players.indexOf(emperor);

        // 通知
        broadcast("皇帝已产生（暗保模式）");

        // 给每个人发身份（私密）
        send(emperor, "你的身份：皇帝");
        send(guard, "你的身份：侍卫（暗保）");

        broadcast("游戏开始，" + players.get(turnIndex) + " 先出牌");

        sendGameStartSnapshot();

    }

    private void checkGameOver() {

        if (gameOver) return;

        // ================= 皇帝方胜 =================
        if (rank.contains(emperor) && rank.contains(guard)) {
            broadcast("皇帝方胜利！");
            gameOver = true;
            return;
        }

        // ================= 平民胜 =================
        int civilianFinish = 0;

        for (String p : rank) {
            if (civilianTeam.contains(p)) {
                civilianFinish++;
            }
        }

        if (civilianFinish >= 3) {
            broadcast("平民方胜利！");
            gameOver = true;
        }
    }

    // ================== 收消息 ==================
    public synchronized void onMessage(String playerId, String msg) {

        if (!started) return;

        if (msg.equals("pass")) {
            pass(playerId);
            return;
        }

        // 简单协议：play:10,10,10
        if (msg.startsWith("play:")) {

            String[] arr = msg.substring(5).split(",");
            List<String> cards = Arrays.asList(arr);

            play(playerId, cards);
        }
    }

    // ================== 过牌 ==================
    private void pass(String playerId) {

        if (!players.get(turnIndex).equals(playerId)) {
            send(playerId, "还没轮到你");
            return;
        }

        if (newRound) {
            send(playerId, "新一轮不能pass");
            return;
        }

        passCount++;

        broadcast(playerId + " 选择过牌");

        // 如果4人都pass
        if (passCount >= players.size() - 1) {
            lastCards.clear();
            passCount = 0;
            newRound = true;

            broadcast("一轮结束，重新出牌！");
        }

        nextTurn();
    }

    // ================== 出牌 ==================
    private void play(String playerId, List<String> cards) {

        if (!players.get(turnIndex).equals(playerId)) {
            send(playerId, "还没轮到你");
            return;
        }

        if (!canPlay(cards)) {
            send(playerId, "出牌不合法");
            return;
        }

        List<String> hand = hands.get(playerId);

        // ================= 最后才能出三规则 =================
        if (!cards.contains("3")) {
            if (hand.size() != usedHands.get(playerId).size() + 1 ) {
                send(playerId, "3只能最后出");
                return;
            }
        }

        // ================= 正常出牌 =================
        hand.removeAll(cards);

        lastCards = cards;
        passCount = 0;
        newRound = false;

        for (int i = 0; i < hand.size(); i++) {
            usedHands.get(playerId).addAll(cards);
        }

        broadcast(playerId + " 出牌：" + cards);

        // 出完后
        if (hand.isEmpty()) {
            rank.add(playerId);
            broadcast(playerId + " 出完了！名次：" + rank.size());
            checkGameOver();
        }

        nextTurn();
    }

    private void nextTurn() {
        turnIndex = (turnIndex + 1) % players.size();

        broadcast("轮到：" + players.get(turnIndex));
    }

    // ================== 出牌规则 ==================
    private boolean canPlay(List<String> cards) {

        if (lastCards.isEmpty()) return true;

        // 张数必须一致
        if (cards.size() != lastCards.size()) return false;

        int newSum = sumValue(cards);
        int lastSum = sumValue(lastCards);

        return newSum > lastSum;
    }

    private int sumValue(List<String> cards) {

        int sum = 0;

        for (String card : cards) {
            sum += value(card);
        }

        return sum;
    }

    // ================== 工具 ==================
    private int value(String card) {

        if (card.startsWith("BJ")) return 12;
        if (card.startsWith("SJ")) return 11;

        String[] parts = card.split("_");
        String rank = parts[1];

        Map<String, Integer> map = new HashMap<>();
        map.put("6",1);
        map.put("7",2);
        map.put("8",3);
        map.put("9",4);
        map.put("10",5);
        map.put("J",6);
        map.put("Q",7);
        map.put("K",8);
        map.put("A",9);
        map.put("2",10);
        map.put("3", 0);

        return map.getOrDefault(rank, 0);
    }

    private void send(String playerId, String msg) {
        Channel ch = channels.get(playerId);
        if (ch != null) {
            ch.writeAndFlush(new TextWebSocketFrame(msg));
        }
    }

    private void send(Channel ch, String msg) {
        ch.writeAndFlush(new TextWebSocketFrame(msg));
    }

    private void broadcast(String msg) {
        channels.values().forEach(ch ->
                ch.writeAndFlush(new TextWebSocketFrame(msg))
        );
    }

    private String buildStartMsg(String playerId) {
        return "start:" + hands.get(playerId).toString();
    }

    private void sendGameStartSnapshot() {
        for (String viewerPlayerId : players) {
            send(viewerPlayerId, GSON.toJson(buildPlayerSnapshot(viewerPlayerId)));
        }
    }

    private List<Map<String, Object>> buildPlayerSnapshot(String viewerPlayerId) {
        List<Map<String, Object>> payload = new ArrayList<>();
        for (String playerId : players) {
            Map<String, Object> playerInfo = new LinkedHashMap<>();
            playerInfo.put("playerId", playerId);
            playerInfo.put("isMe", playerId.equals(viewerPlayerId));
            playerInfo.put("identity", resolveIdentity(playerId));
            playerInfo.put("cards", formatCardsForClient(hands.getOrDefault(playerId, Collections.emptyList())));
            payload.add(playerInfo);
        }
        return payload;
    }

    private String resolveIdentity(String playerId) {
        if (playerId == null) {
            return "平民";
        }
        if (playerId.equals(emperor)) {
            return "皇帝";
        }
        if (playerId.equals(guard)) {
            return "侍卫(暗保)";
        }
        return "平民";
    }

    private List<String> formatCardsForClient(List<String> rawCards) {
        List<String> cards = new ArrayList<>(rawCards.size());
        for (String rawCard : rawCards) {
            cards.add(formatCardForClient(rawCard));
        }
        return cards;
    }

    private boolean containsCardWithPrefix(List<String> hand, String prefix) {
        for (String card : hand) {
            if (card != null && card.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String formatCardForClient(String rawCard) {
        if (rawCard == null || rawCard.isBlank()) {
            return rawCard;
        }
        String[] parts = rawCard.split("_");
        if (parts.length == 2) {
            return rawCard;
        }
        if (parts.length == 3) {
            return parts[1] + "_" + parts[0] + "_" + parts[2];
        }
        return rawCard;
    }

    // ================== 生成168张牌 ==================
    private List<String> generateDeck() {
        String[] suits = {"S", "H", "D", "C"}; // 黑桃 红心 方块 梅花
        String[] ranks = {"6","7","8","9","10","J","Q","K","A","2"};
        List<String> deck = new ArrayList<>();
        int id = 0;
        // ================= 4副牌 =================
        for (int i = 0; i < 4; i++) {

            // 普通牌
            for (String suit : suits) {
                for (String rank : ranks) {
                    deck.add(suit + "_" + rank + "_" + (id++));
                }
            }

            // 小王（SJ）
            deck.add("SJ_" + (id++));

            // 大王（BJ）
            deck.add("BJ_" + (id++));
        }

        return deck;
    }

    private String getRandomSuit() {
        String[] suits = {"S", "H", "D", "C"};
        return suits[new Random().nextInt(4)];
    }
}
