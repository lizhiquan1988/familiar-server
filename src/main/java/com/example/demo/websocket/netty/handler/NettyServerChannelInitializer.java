package com.example.demo.websocket.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class NettyServerChannelInitializer extends ChannelInitializer<Channel> {

    private final NettyServerHandler nettyServerHandler;

    private final PokerGameHandler pokerGameHandler;

    public NettyServerChannelInitializer(NettyServerHandler nettyServerHandler, PokerGameHandler pokerGameHandler) {
        this.nettyServerHandler = nettyServerHandler;
        this.pokerGameHandler = pokerGameHandler;
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new ChunkedWriteHandler());

        // 心跳
        pipeline.addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));

        // 路径分发前 使用
        pipeline.addLast(new MultiPathHandler(this.nettyServerHandler, this.pokerGameHandler));

        pipeline.addLast(new HeartBeatHandler());

    }
}
