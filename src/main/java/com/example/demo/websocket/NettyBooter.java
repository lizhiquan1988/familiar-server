package com.example.demo.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NettyBooter implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private NettyServer nettyServer;

    /**
     * 当所有的bean都已经处理完毕之后，spring ioc容器会有一个发布事件动作
     *
     * @param event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event){
    	nettyServer.start();
        log.info("NettyServerr启动");
    }
}
