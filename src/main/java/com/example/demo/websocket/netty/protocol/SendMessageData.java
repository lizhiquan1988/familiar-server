package com.example.demo.websocket.netty.protocol;

import lombok.Data;

@Data
public class SendMessageData {
    private static final long serialVersionUID = 1234213567896453167L;
    private String addr; // IP地址以及端口
    private String cmd; // 命令类型 SYSTEM|LOGIN|LOGOUT|CHAT|FLOWER|KEEPALIVE
    private long time; // 命令发送时间
    private int online; // 当前在线人数
    private String sender;// 发送人
    private String receiver;// 接收人
    private String content; // 消息内容
    private String terminal; // 终端
    public SendMessageData(){}


    public SendMessageData(String cmd,long time,int online,String sender,String content){
        this.cmd = cmd;
        this.time = time;
        this.online = online;
        this.sender = sender;
        this.content = content;
    }

    // 聊天
    public SendMessageData(String cmd,long time,String sender,String content){
        this.cmd = cmd;
        this.time = time;
        this.sender = sender;
        this.content = content;
    }


    @Override
    public String toString() {
        return "SendMessageData{" +
                "addr='" + addr + '\'' +
                ", cmd='" + cmd + '\'' +
                ", time=" + time +
                ", online=" + online +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
