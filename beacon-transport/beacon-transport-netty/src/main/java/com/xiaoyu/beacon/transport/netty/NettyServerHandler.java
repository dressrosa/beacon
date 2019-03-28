/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.beacon.transport.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.beacon.common.constant.From;
import com.xiaoyu.beacon.transport.api.BeaconHandler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 2017年4月20日下午3:15:38
 * 
 * @author xiaoyu
 * @description
 */
@Sharable
public class NettyServerHandler extends ChannelDuplexHandler {

    private static final Logger LOG = LoggerFactory.getLogger(NettyServerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (!ctx.channel().isActive()) {
            NettyChannel.removeChannel(ctx.channel());
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 处理心跳
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                LOG.info("Server receive read timeout heartbeat,close channel.");
                NettyChannel.removeChannel(ctx.channel());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.disconnect(ctx, promise);
        if (!ctx.channel().isActive()) {
            NettyChannel.removeChannel(ctx.channel());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BeaconHandler handler = NettyChannel.getChannel(ctx.channel(), From.SERVER);
        try {
            handler.receive(msg);
        } finally {
            if (!ctx.channel().isActive()) {
                NettyChannel.removeChannel(ctx.channel());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 在client发送消息来之后,在server端返回消息前(一般在请求超时的时候client挂了),client已经关闭,这样就会
        // 造成 Connection reset by peer的错误,这里去除该channel
        LOG.error("Exception catch in channel:{}", cause);
        if (!ctx.channel().isActive()) {
            NettyChannel.removeChannel(ctx.channel());
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }
}
