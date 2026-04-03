package com.example.demo.websocket.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class MultiPathHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static final AttributeKey<String> ROOM_ID = AttributeKey.valueOf("roomId");
    public static final AttributeKey<String> PLAYER_ID = AttributeKey.valueOf("playerId");

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final NettyServerHandler nettyServerHandler;
    private final PokerGameHandler pokerGameHandler;

    public MultiPathHandler(NettyServerHandler nettyServerHandler, PokerGameHandler pokerGameHandler) {
        this.nettyServerHandler = nettyServerHandler;
        this.pokerGameHandler = pokerGameHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
        String path = decoder.path();
        Map<String, List<String>> params = decoder.parameters();
        ChannelPipeline pipeline = ctx.pipeline();

        setAttributeIfPresent(ctx, ROOM_ID, firstParam(params, "roomId"));
        setAttributeIfPresent(ctx, PLAYER_ID, firstParam(params, "playerId"));
        logger.info("path: {}, roomId: {}, playerId: {}", path, ctx.channel().attr(ROOM_ID).get(), ctx.channel().attr(PLAYER_ID).get());

        if ("/audio".equals(path)) {
            addWebSocketRoute(pipeline, "/audio", nettyServerHandler);
        } else if ("/poker".equals(path)) {
            if (isBlank(ctx.channel().attr(ROOM_ID).get()) || isBlank(ctx.channel().attr(PLAYER_ID).get())) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
                return;
            }
            addWebSocketRoute(pipeline, "/poker", pokerGameHandler);
        } else {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            return;
        }

        pipeline.remove(this);
        ctx.fireChannelRead(req.retain());
    }

    private static String firstParam(Map<String, List<String>> params, String key) {
        List<String> values = params.get(key);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    private static void setAttributeIfPresent(ChannelHandlerContext ctx, AttributeKey<String> key, String value) {
        if (value != null && !value.isEmpty()) {
            ctx.channel().attr(key).set(value);
        }
    }

    private static void addWebSocketRoute(ChannelPipeline pipeline, String path, ChannelHandler handler) {
        pipeline.addLast(new WebSocketServerProtocolHandler(path, null, true));
        pipeline.addLast(handler);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }

        ChannelFuture future = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
