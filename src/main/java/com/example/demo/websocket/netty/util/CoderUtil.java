package com.example.demo.websocket.netty.util;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.demo.websocket.netty.protocol.MsgActionEnum;
import com.example.demo.websocket.netty.protocol.SendMessageData;
import com.example.demo.websocket.netty.protocol.SendMessageDataForAiSpeaker;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CoderUtil {

    // 消息头 -
    private static Pattern pattern = Pattern.compile("^\\[(.*)\\](\\s\\-\\s(.*))?");

    private static final Gson gson = new Gson();

    public static SendMessageData decode(String msg){
        if(msg == null || "".equals(msg)){
            return null;
        }
        try{
            Matcher m = pattern.matcher(msg);
            // [命令][命令发送时间][命令发送人][终端类型] - 内容
            String header = "";
            String content = "";
            if(m.matches()){
                header = m.group(1);
                content = m.group(3);
            }
            String[] headers = header.split("\\]\\[");
            long time = 0;
            try{
            	if(headers.length > 2) {
            		time =  Long.parseLong(headers[1]);
            	} else {
            		time = System.currentTimeMillis();
            	}
                
            }catch (Exception e){
                System.err.println("时间转化出现异常：" + e);
            }
            String nickName = headers.length >= 3 ? headers[2] : "guest";
            //昵称最多是是个字
            nickName = nickName.length() < 10 ? nickName: nickName.substring(0,9);
            String cmd = headers[0];

            if(msg.startsWith("[" + MsgActionEnum.LOGIN.getName() + "]") ||
                    msg.startsWith("[" + MsgActionEnum.LOGOUT.getName() + "]")){
                return new SendMessageData(cmd,time,nickName,content);
            }else if(msg.startsWith("[" + MsgActionEnum.CHAT.getName() + "]")){
                return new SendMessageData(cmd,time,nickName,content);
            }else if(msg.startsWith("[" + MsgActionEnum.KEEPALIVE.getName() + "]") ||
                    msg.startsWith("[" + MsgActionEnum.KEEPALIVE.getName() + "]")){
                return new SendMessageData(cmd,time,null,nickName);
            }else if (msg.startsWith("[" + MsgActionEnum.UPDATELOCATION.getName() + "]")) {
            	return new SendMessageData(cmd,time,nickName,content);
            }
//            else if(msg.startsWith("[" + MsgActionEnum.FLOWER.getName() + "]")){
//                return new IMMessage(cmd,headers[3],time,nickName);
//            }else if(msg.startsWith("[" + MsgActionEnum.KEEPALIVE.getName() + "]") ||
//                    msg.startsWith("[" + MsgActionEnum.KEEPALIVE.getName() + "]")){
//                return new IMMessage(cmd,headers[3],time,nickName);
//            }
            else{
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    /***
     * 将IMMessage对象编码成指定的自定义协议字符串
     * @param msg
     * @return
     */
    public static String encode(SendMessageData msg){
        if(null == msg){
            return "";
        }
        String cmd = msg.getCmd();
        String sender = msg.getSender();
        String prex = "[" +  cmd +"]" + "[" + msg.getTime() +"]";
        if(MsgActionEnum.LOGIN.getName().equals(cmd) || MsgActionEnum.FLOWER.getName().equals(cmd)){
            prex += ("[" + sender + "][" + msg.getTerminal() + "]");
        }else if(MsgActionEnum.CHAT.getName().equals(cmd) ){
            prex += ("[" + sender + "]");
        }else if(MsgActionEnum.SYSTEM.getName().equals(cmd)){
            prex += ("[" + msg.getOnline() + "]");
        }
        String content = msg.getContent();
        if (content != null && !"".equals(content)) {
            prex += (" - " + msg.getContent());
        }
        return prex;
    }

    /**
     * 将 SendMessageDataForAiSpeaker 转为 JSON 字符串
     * 输出格式：
     * {"cmd":"GPT_RESPONSE","msg":"you can over Websocket.","content":"AI回答内容"}
     */
    public static String encodeToJson(SendMessageDataForAiSpeaker msg) {
        if (msg == null) {
            return "{}";
        }

        JsonObject json = new JsonObject();

        if (msg.getCmd() != null) json.addProperty("cmd", msg.getCmd());
        if (msg.getMsg() != null) json.addProperty("msg", msg.getMsg());
        if (msg.getContent() != null) json.addProperty("content", msg.getContent());
        if (msg.getTime() > 0) json.addProperty("time", msg.getTime());
        if (msg.getSize() > 0) json.addProperty("size", msg.getSize());
        if (msg.getAddr() != null) json.addProperty("addr", msg.getAddr());
        if (msg.getUrl() != null) json.addProperty("url", msg.getUrl());

        return gson.toJson(json);
    }

    /**
     * 从 JSON 字符串解析成 SendMessageDataForAiSpeaker 对象
     */
    public static SendMessageDataForAiSpeaker decodeFromJson(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return null;
        }

        try {
            JsonObject obj = JsonParser.parseString(jsonStr).getAsJsonObject();
            SendMessageDataForAiSpeaker msg = new SendMessageDataForAiSpeaker();

            if (obj.has("cmd")) msg.setCmd(obj.get("cmd").getAsString());
            if (obj.has("msg")) msg.setMsg(obj.get("msg").getAsString());
            if (obj.has("content")) msg.setContent(obj.get("content").getAsString());
            if (obj.has("addr")) msg.setAddr(obj.get("addr").getAsString());
            if (obj.has("time")) msg.setTime(obj.get("time").getAsLong());
            if (obj.has("size")) msg.setSize(obj.get("size").getAsLong());
            if (obj.has("url")) msg.setContent(obj.get("url").getAsString());

            return msg;
        } catch (Exception e) {
            System.err.println("❌ JSON 解析失败: " + e.getMessage());
            return null;
        }
    }

}