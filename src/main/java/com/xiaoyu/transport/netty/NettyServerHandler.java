/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.transport.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(NettyServerHandler.class);

    private BeaconHandler beaconHandler;

    public NettyServerHandler(BeaconHandler beaconHandler) {
        this.beaconHandler = beaconHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        LOG.info("server channel初始化:{}", ctx.channel().id().asLongText());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BaseChannel beaconChannel = BeaconServerChannel.getChannel(ctx.channel());
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
        // super.write(ctx, msg, promise);
        BaseChannel beaconChannel = BeaconServerChannel.getChannel(ctx.channel());
        try {
            this.beaconHandler.send(msg, beaconChannel);
        } finally {
            if (!ctx.channel().isActive()) {
                ((BeaconServerChannel) beaconChannel).removeChannel(ctx.channel());
            }
        }

    }
}
