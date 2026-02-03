package com.example.demo.websocket.netty.protocol;

public enum MsgActionEnum {
    SYSTEM("SYSTEM","系统消息"),

    LOGIN("LOGIN","登录指令"),
    LOGOUT("LOGOUT","登出指令"),
    UPDATELOCATION("UPDATELOCATION", "位置更新"),
    STOPUPDATE("STOP_UPDATE", "位置更新停止"),
    CHAT("CHAT","聊天消息"),
    FLOWER("FLOWER","送鲜花"),
    GPTRESPONSE("GPTRESPONSE","ChatAI返回結果"),
    FINISH("FINISH","前端录音结束"),
    AUDIO_CANCEL("AUDIO_CANCEL", "取消录音"),
    LANGUAGE("LANGUAGE","使用的语言"),
    CHANGELANGUAGE("CHANGELANGUAGE","切换语言"),
    CLEARAIHISTORY("CLEARAIHISTORY","清除AI历史"),
    GETAIHISTORY("GETAIHISTORY","获取AI聊天记录条数"),
    RESETMEDICATIONTIME("RESETMEDICATIONTIME","重置吃药提醒"),
    KEEPALIVE("KEEPALIVE","心跳检测"),
    HEARTBEAT("HEARTBEAT","心跳检测");

    private String name ;
    private String msg;

    MsgActionEnum(String name, String msg) {
        this.name = name;
        this.msg = msg;
    }

    /**
     * 判断是否是我们规定的协议指令
     * @param
     * @return
     */
    public static boolean isIMP(String content){
        return content.matches("^\\[(SYSTEM|LOGIN|LOGOUT|CHAT|FLOWER)\\]");
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}