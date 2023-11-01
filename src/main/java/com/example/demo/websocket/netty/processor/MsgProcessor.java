package com.example.demo.websocket.netty.processor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

import com.alibaba.fastjson2.JSONObject;
import com.example.demo.websocket.netty.protocol.MsgActionEnum;
import com.example.demo.websocket.netty.protocol.SendMessageData;
import com.example.demo.websocket.netty.util.CoderUtil;


@Slf4j
public class MsgProcessor {
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

    // 定义一些扩展属性
    //昵称
    public static final AttributeKey<String> NICK_NAME = AttributeKey.valueOf("nickName");
    // ip地址
    public static final AttributeKey<String> IP_ADDR = AttributeKey.valueOf("ipAddr");
    // 其他扩展属性
    public static final AttributeKey<JSONObject> ATTRS = AttributeKey.valueOf("attrs");
    // 终端
    public static final AttributeKey<String> TERMINAL = AttributeKey.valueOf("terminal");


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
}