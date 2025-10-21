package com.example.demo.websocket.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("rawtypes")
public class NettyServerChannelInitializer extends ChannelInitializer {

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new ChunkedWriteHandler());
        // 心跳机制
        pipeline.addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));

        // WebSocket 协议处理
        pipeline.addLast(new WebSocketServerProtocolHandler("/audio", null, true));

        pipeline.addLast(new HeartBeatHandler());
        pipeline.addLast(new NettyServerHandler());
    }
}
