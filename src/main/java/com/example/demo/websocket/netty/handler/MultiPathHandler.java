package com.example.demo.websocket.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class MultiPathHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final NettyServerHandler nettyServerHandler;

    private final PokerGameHandler pokerGameHandler;

    public MultiPathHandler(NettyServerHandler nettyServerHandler, PokerGameHandler pokerGameHandler) {
        this.nettyServerHandler = nettyServerHandler;
        this.pokerGameHandler = pokerGameHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        String uri = req.uri();
        ChannelPipeline pipeline = ctx.pipeline();
        logger.info("完整path："+uri);

        // 1. パスによって「プロトコルハンドラー」と「ビジネスロジックハンドラー」をセットで追加
        if ("/audio".equals(uri)) {
            pipeline.addLast(new WebSocketServerProtocolHandler("/audio", null, true));
            pipeline.addLast(this.nettyServerHandler); // オーディオ専用
        }
        else if ("/poker".equals(uri)) {
            pipeline.addLast(new WebSocketServerProtocolHandler("/poker", null, true));
            pipeline.addLast(pokerGameHandler); // game専用
        }
        else if ("/default".equals(uri)) {
            pipeline.addLast(new WebSocketServerProtocolHandler("/default", null, true));
            // 元々使っていた nettyServerHandler をここで追加（共有可能な場合は使い回し）
            pipeline.addLast();
        }
        else {
            // 許可されていないパス
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            return;
        }

        // 2. 共通のハンドラー（HeartBeatなど）が最後に来るように調整が必要な場合はここで行う
        // もし initChannel で既に追加されているなら、その手前に追加するように工夫します。

        // 3. この判定用ハンドラー自身を削除
        pipeline.remove(this);

        // 4. 次のハンドラーへデータを渡す（retainが必要）
        ctx.fireChannelRead(req.retain());
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // レスポンスステータスが 200 OK 以外の場合、エラー内容をコンテンツに書き込む
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }

        // 接続を維持するか、閉じるかを判定して送信
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
