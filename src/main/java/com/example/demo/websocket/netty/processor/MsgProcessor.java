package com.example.demo.websocket.netty.processor;

import com.example.demo.Component.GlobalState;
import com.example.demo.Component.TranscriptionTaskManager;
import com.example.demo.controller.SpeechToText.AssemblyAiController;
import com.example.demo.service.SpeechToText.AssemblyAiService;
import com.example.demo.utils.JapanLocalTime;
import com.example.demo.utils.SpringContextUtil;
import com.example.demo.websocket.netty.protocol.SendMessageDataForAiSpeaker;
import com.example.demo.websocket.netty.util.SendMessageToFront;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.fastjson2.JSONObject;
import com.example.demo.websocket.netty.protocol.MsgActionEnum;
import com.example.demo.websocket.netty.protocol.SendMessageData;
import com.example.demo.websocket.netty.util.CoderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MsgProcessor {

    @Autowired
    private GlobalState globalState;

    // 记录同时在线人数
    // ChannelGroup是一个线程安全的集合，它提供了打开一个Channel和不同批量的方法。
    // 可以使用ChannelGroup来将Channel分类到一个有特别意义的组中。
    // 当组中的channel关闭时会自动从组中移除，因此我们不需要担心添加进去的channel的生命周期。
    public static ChannelGroup onlineUsers = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    // 由于我们这里会处理http中对应的channel，所以在关闭的时候我们需要判断这个关闭的channl是不是我们socket对应的
    // channl，而ChannelGroup在channel关闭时会自动从组中移除，所以我们这里单独记录一下，这样我们可以判断如果某个
    // socket对应的channel关闭，我们可以通知其他人某人下线
    // 实际工作中我们都是前后端分离，不会出现这个现象
    public static Set onlineUserSet = new HashSet();

    private static final ConcurrentHashMap<String, ByteArrayOutputStream> audioBufferMap = new ConcurrentHashMap<>();

    private static final String AUDIO_SAVE_DIR = "/www/wwwroot/myapp/static/audio/cache"; // Linux 下 /tmp，或改成 /www/server/audio

    // 定义一些扩展属性
    //昵称
    public static final AttributeKey<String> NICK_NAME = AttributeKey.valueOf("nickName");
    // ip地址
    public static final AttributeKey<String> IP_ADDR = AttributeKey.valueOf("ipAddr");
    // 其他扩展属性
    public static final AttributeKey<JSONObject> ATTRS = AttributeKey.valueOf("attrs");
    // 终端
    public static final AttributeKey<String> TERMINAL = AttributeKey.valueOf("terminal");

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ExecutorService executor = Executors.newCachedThreadPool();

    static {
        File dir = new File(AUDIO_SAVE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 发送消息（netty）
     *
     * @param ctx
     * @param msg
     */
    public void dealMsg(ChannelHandlerContext ctx, Object msg) {
        // 编解码
        SendMessageData decode = CoderUtil.decode(msg.toString());
        dealMsg(ctx, decode);
    }

    public void dealMsg(ChannelHandlerContext ctx, SendMessageData msg) {
        if (msg == null) {
            return;
        }
        Channel client = ctx.channel();
        String addr = getAddress(client);
        log.info(addr);
        String cmd = msg.getCmd();
        if (cmd.equals(MsgActionEnum.LOGIN.getName())) {
            // 设置一些属性
            client.attr(NICK_NAME).getAndSet(msg.getSender());
            client.attr(IP_ADDR).getAndSet(addr);
            client.attr(TERMINAL).getAndSet(msg.getTerminal());
            // 把这个用户保存一个统一容器中，方便给所有用户发送消息
            onlineUsers.add(client);
            onlineUserSet.add(client.id());
            for (Channel channel : onlineUsers) {
                boolean isSelf = (channel == client);
                System.out.println(msg);
                if (isSelf) {
                    msg = new SendMessageData(MsgActionEnum.SYSTEM.getName(), msg.getTime(), onlineUsers.size(), null, "已与服务器建立连接！");
                } else {
                    msg = new SendMessageData(MsgActionEnum.SYSTEM.getName(), msg.getTime(), onlineUsers.size(), getNickName(client), getNickName(client) + "加入");
                }
                String content = CoderUtil.encode(msg);
                channel.writeAndFlush(content);
            }

        } else if (cmd.equals(MsgActionEnum.CHAT.getName())) {
            String sender = msg.getSender();
            for (Channel channel : onlineUsers) {
                boolean isSelf = (channel == client);
                if (isSelf) {
                    msg.setSender("you");
                } else {
                    msg.setSender(sender);
                }
                String content = CoderUtil.encode(msg);
                channel.writeAndFlush(content);
            }

        } else if (cmd.equals(MsgActionEnum.LOGOUT.getName())) {
            client.close();
            onlineUserSet.remove(client.id());
            for (Channel channel : onlineUsers) {
                msg.setCmd(MsgActionEnum.SYSTEM.getName());
                msg.setOnline(onlineUsers.size());
                msg.setContent(getNickName(client) + "已经退出群聊");
                String content = CoderUtil.encode(msg);
                channel.writeAndFlush(content);
            }
        } else if (cmd.equals(MsgActionEnum.UPDATELOCATION.getName())) {
        	for (Channel channel : onlineUsers) {
        		if("1001".equals(channel.id())) {
        			msg.setCmd(MsgActionEnum.UPDATELOCATION.getName());
                    msg.setOnline(onlineUsers.size());
                    msg.setContent("update");
                    String content = CoderUtil.encode(msg);
                    channel.writeAndFlush(content);
        		}
            }
        } else if (cmd.equals(MsgActionEnum.STOPUPDATE.getName())) {
        	for (Channel channel : onlineUsers) {
        		if("1001".equals(channel.id())) {
        			msg.setCmd(MsgActionEnum.UPDATELOCATION.getName());
                    msg.setOnline(onlineUsers.size());
                    msg.setContent("stop_update");
                    String content = CoderUtil.encode(msg);
                    channel.writeAndFlush(content);
        		}
            }
        } else if (cmd.equals(MsgActionEnum.KEEPALIVE.getName())) {
            log.info("收到来自channelId为[" + client.id() + "]的心跳包...");
        }
    }

    /**
     * 处理音频块
     */
    public void dealAudioChunk(ChannelHandlerContext ctx, byte[] audioChunk) {
        Channel client = ctx.channel();
        String clientId = client.id().asShortText();

        // 获取或创建缓存区
        ByteArrayOutputStream buffer = audioBufferMap.computeIfAbsent(clientId, k -> new ByteArrayOutputStream());
        try {
            buffer.write(audioChunk);
        } catch (IOException e) {
            log.error("音频写入缓存失败: {}", e.getMessage());
        }

        log.info("收到来自 {} 的音频块：{} bytes，当前缓存大小：{}", clientId, audioChunk.length, buffer.size());

        // 返回确认消息（可选）
        SendMessageDataForAiSpeaker sendMessage = new SendMessageDataForAiSpeaker(
                "AUDIO_ACK",
                JapanLocalTime.getJapanNowTimestampSeconds(),
                "chunk received.",
                audioChunk.length,
                "","");
        SendMessageToFront.sendTo(sendMessage, logger);
    }

    /**
     * 停止录音并保存文件
     */
    public void finishRecording(ChannelHandlerContext ctx) {
        Channel client = ctx.channel();
        String clientId = client.id().asShortText();

        if (clientId == null) {
            log.warn("clientId为空，无法完成录音结束操作");
            return;
        }

        ByteArrayOutputStream buffer = audioBufferMap.remove(clientId);
        if (buffer == null) {
            log.warn("没有找到 {} 的音频缓存", clientId);
            return;
        }

        // 保存文件
        String fileName = "record_" + clientId + ".wav"; //+ System.currentTimeMillis() + ".wav";
        File file = new File(AUDIO_SAVE_DIR, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] data = buffer.toByteArray();
            // TODO 以下是ESP32发送的数据必须的处理
            addWavHeader(buffer.toByteArray(), 16000, 1,16);
            fos.write(data);
            fos.flush();
            log.info("音频保存完成：{}", file.getAbsolutePath());

            // 回复客户端
            SendMessageDataForAiSpeaker sendMessage = new SendMessageDataForAiSpeaker(
                    "AUDIO_SAVE",
                    JapanLocalTime.getJapanNowTimestampSeconds(),
                    "",
                    data.length,
                    file.getAbsolutePath(),"");
            SendMessageToFront.sendTo(sendMessage, logger);

            executor.submit(() -> {
                // ✅ 获取 Controller Bean
                AssemblyAiController controller = SpringContextUtil.getBean(AssemblyAiController.class);

                // ✅ 直接调用 transcribe() 方法
                Map<String, Object> result = controller.transcribe(data);

                log.info("AssemblyAI 任务提交结果: {}", result);
            });

            log.info("AssemblyAI 在另一个线程执行中");

        } catch (IOException e) {
            log.error("保存音频文件失败: {}", e.getMessage());
        }
    }

    /**
     * 录音取消（丢弃缓存）
     */
    public void cancelRecording(ChannelHandlerContext ctx) {
        Channel client = ctx.channel();
        String clientId = client.id().asShortText();
        audioBufferMap.remove(clientId);
        log.info("录音已取消：{}", clientId);
    }

    /**
     * 获取用户昵称
     *
     * @param client
     * @return
     */
    private String getNickName(Channel client) {
        return client.attr(NICK_NAME).get();
    }

    /**
     * 获取扩展属性
     *
     * @param client
     * @param key
     * @param value
     */
    private void setAttrs(Channel client, String key, long value) {
        try {
        	JSONObject json = client.attr(ATTRS).get();
            if (json == null) {
                json = new JSONObject();
            }
            json.put(key, value);
            client.attr(ATTRS).set(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取扩展属性
     *
     * @param client
     * @return
     */
    private JSONObject getAttrs(Channel client) {
        try {
            return client.attr(ATTRS).get();
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * 获取用户远程IP
     *
     * @param client
     * @return
     */
    public String getAddress(Channel client) {
        return client.remoteAddress().toString().replaceFirst("/", "");
    }

    /**
     * 获取系统时间
     *
     * @return
     */
    private Long sysTime() {
        return System.currentTimeMillis();
    }


    /**
     * 当客户端断开或者关闭时候，我们需要移除channel
     */
    public void removeChannelSendMsg(Channel client) {
        String nickName = getNickName(client);

        //如果没有就说明不是对应websocket请求就不需要发送信息
        if (!onlineUserSet.remove(client.id())) {
            return;
        }
        log.info("客户端被移除，channelId为：" + client.id().asShortText());

        for (Channel channel : onlineUsers) {
            SendMessageData msg = new SendMessageData(MsgActionEnum.SYSTEM.getName(), sysTime(), onlineUsers.size(), nickName,nickName + "已经退出群聊");

            String content = CoderUtil.encode(msg);
            channel.writeAndFlush(content);
        }
    }

    private byte[] addWavHeader(byte[] pcmData, int sampleRate, int channels, int bitsPerSample) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        int totalDataLen = pcmData.length + 36;
        int totalAudioLen = pcmData.length;

        out.write("RIFF".getBytes());
        out.write(intToByteArray(totalDataLen), 0, 4);
        out.write("WAVE".getBytes());
        out.write("fmt ".getBytes());
        out.write(intToByteArray(16), 0, 4); // PCM header size
        out.write(shortToByteArray((short)1), 0, 2); // PCM format
        out.write(shortToByteArray((short)channels), 0, 2);
        out.write(intToByteArray(sampleRate), 0, 4);
        out.write(intToByteArray(byteRate), 0, 4);
        out.write(shortToByteArray((short)(channels * bitsPerSample / 8)), 0, 2);
        out.write(shortToByteArray((short)bitsPerSample), 0, 2);
        out.write("data".getBytes());
        out.write(intToByteArray(totalAudioLen), 0, 4);
        out.write(pcmData);

        return out.toByteArray();
    }

    private byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value),
                (byte)(value >> 8),
                (byte)(value >> 16),
                (byte)(value >> 24)
        };
    }

    private byte[] shortToByteArray(short value) {
        return new byte[] {
                (byte)(value),
                (byte)(value >> 8)
        };
    }

}