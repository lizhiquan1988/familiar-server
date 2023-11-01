package com.example.demo.websocket.netty.handler;


import com.example.demo.websocket.netty.processor.MsgProcessor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyServerHandler extends SimpleChannelInboundHandler<String>{

    private MsgProcessor processor = new MsgProcessor();
    
    private static ChannelHandlerContext channelHandlerContext;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelHandlerContext = ctx;
        System.out.println("客户端 " + channel.remoteAddress() + "上线\n");
    }

    /**
     * 业务处理
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        processor.dealMsg(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 发生异常之后关闭连接（关闭channel），随后从ChannelGroup中移除
        ctx.channel().close();
    }

    /**
     * 客户端程序关闭则移除对应的channel
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        processor.removeChannelSendMsg(ctx.channel());
    }
    
    public static void sendMessage(String message) throws Exception {
    	if(channelHandlerContext != null) {
    		Channel client = channelHandlerContext.channel();
    		System.out.println("客户端 " + client.remoteAddress() + "に発送："+message);
    		client.writeAndFlush(message);
    	}
    }
}
