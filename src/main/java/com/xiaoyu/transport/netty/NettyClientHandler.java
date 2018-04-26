/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.transport.netty;

import com.xiaoyu.core.common.constant.From;
import com.xiaoyu.transport.api.BeaconHandler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * 2017年4月20日下午3:15:38
 * 
 * @author xiaoyu
 * @description
 */
@Sharable
public class NettyClientHandler extends ChannelDuplexHandler {

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
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.disconnect(ctx, promise);
        if (!ctx.channel().isActive()) {
            NettyChannel.removeChannel(ctx.channel());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BeaconHandler handler = NettyChannel.getChannel(ctx.channel(), From.CLIENT);
        try {
            // 交给上层处理
            handler.receive(msg);
        } finally {
            if (!ctx.channel().isActive()) {
                NettyChannel.removeChannel(ctx.channel());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }

}
