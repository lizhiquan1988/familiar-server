package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.websocket.netty.handler.NettyServerHandler;
import com.example.demo.websocket.netty.protocol.MsgActionEnum;
import com.example.demo.websocket.netty.protocol.SendMessageData;
import com.example.demo.websocket.netty.util.CoderUtil;

import jakarta.servlet.http.HttpServletResponse;

@CrossOrigin(origins = "https://www.mimamaori.tech")
@RestController
public class WebSocketController {
    @RequestMapping(value = "/webSocket/{cmd}")
    public String test(@PathVariable("cmd") String cmd){
    	SendMessageData sendMessageData = new SendMessageData(MsgActionEnum.UPDATELOCATION.getName(), System.currentTimeMillis(),"1001", cmd);
    	Map<String, Object> response = new HashMap<>();
    	try {
    		NettyServerHandler.sendMessage(CoderUtil.encode(sendMessageData));
    	} catch(Exception e) {
    		response.put("code", HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.put("msg", "消息发送失敗");
            return response.toString();
    	}
        response.put("code", HttpServletResponse.SC_OK);
        response.put("msg", "消息发送成功");
        return response.toString();
    	
    }
    
}
