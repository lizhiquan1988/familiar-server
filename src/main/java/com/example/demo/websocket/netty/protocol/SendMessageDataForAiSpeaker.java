package com.example.demo.websocket.netty.protocol;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageDataForAiSpeaker {
    private static final long serialVersionUID = 1234213567896453167L;
    private String addr; // IP地址以及端口
    private String cmd; // 命令类型 SYSTEM|LOGIN|LOGOUT|CHAT|FLOWER|KEEPALIVE
    private long time; // 命令发送时间
    private String content; // 消息内容
    private String msg;
    private String url;
    private long size;
    public SendMessageDataForAiSpeaker(){}

    public SendMessageDataForAiSpeaker(String cmd, long time, String msg, long size, String content, String url){
        this.cmd = cmd;
        this.time = time;
        this.msg = msg;
        this.size = size;
        this.content = content;
        this.url = url;
    }


    @Override
    public String toString() {
        return "SendMessageDataForAiSpeaker{" +
                "addr='" + addr + '\'' +
                ", cmd='" + cmd + '\'' +
                ", time=" + time +
                ", content='" + content + '\'' +
                ", msg='" + msg + '\'' +
                ", size='" + size + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
