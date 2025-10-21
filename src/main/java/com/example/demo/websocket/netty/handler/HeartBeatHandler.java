package com.example.demo.websocket.netty.handler;

import com.example.demo.websocket.netty.processor.MsgProcessor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    private int readIdleTimes = 0;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (!(evt instanceof IdleStateEvent event)) return;

        ChannelId channelId = ctx.channel().id();
        if(!MsgProcessor.onlineUserSet.contains(channelId)) return;

        switch (event.state()){
            case READER_IDLE:
                readIdleTimes++;
                log.debug("读空闲计数: {} - {}", readIdleTimes, ctx.channel().remoteAddress());
                if(readIdleTimes >= 60){
                    log.info("关闭超时 channel: {}", ctx.channel().remoteAddress());
                    ctx.channel().close();
                    // TODO 以后在前端关闭比较好
                }
                break;
            case WRITER_IDLE:
                log.debug("写空闲: {}", ctx.channel().remoteAddress());
                break;
            case ALL_IDLE:
                log.debug("读写空闲: {}", ctx.channel().remoteAddress());
                break;
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 只要有数据读到了，就重置读空闲计数
        readIdleTimes = 0;
        super.channelRead(ctx, msg);
    }
}
