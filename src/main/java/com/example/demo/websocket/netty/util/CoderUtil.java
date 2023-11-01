package com.example.demo.websocket.netty.util;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.demo.websocket.netty.protocol.MsgActionEnum;
import com.example.demo.websocket.netty.protocol.SendMessageData;

public class CoderUtil {

    // 消息头 -
    private static Pattern pattern = Pattern.compile("^\\[(.*)\\](\\s\\-\\s(.*))?");


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
}