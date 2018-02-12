/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.transport.netty;

import com.xiaoyu.transport.BaseChannel;
import com.xiaoyu.transport.BeaconHandler;
import com.xiaoyu.transport.BeaconServerChannel;

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
public class NettyServerHandler extends ChannelDuplexHandler {

    private BeaconHandler beaconHandler;

    public NettyServerHandler(BeaconHandler beaconHandler) {
        this.beaconHandler = beaconHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BaseChannel beaconChannel = BeaconServerChannel.getChannel(ctx.channel(), beaconHandler);
        try {
            this.beaconHandler.received(msg, beaconChannel);
        } finally {
            if (!ctx.channel().isActive()) {
                ((BeaconServerChannel) beaconChannel).removeChannel(ctx.channel());
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
