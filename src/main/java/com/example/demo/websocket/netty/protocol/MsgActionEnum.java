package com.example.demo.websocket.netty.protocol;

public enum MsgActionEnum {
    SYSTEM("SYSTEM","系统消息"),

    LOGIN("LOGIN","登录指令"),
    LOGOUT("LOGOUT","登出指令"),
    UPDATELOCATION("UPDATELOCATION", "位置更新"),
    STOPUPDATE("STOP_UPDATE", "位置更新停止"),
    CHAT("CHAT","聊天消息"),
    FLOWER("FLOWER","送鲜花"),
    KEEPALIVE("KEEPALIVE","心跳检测");

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