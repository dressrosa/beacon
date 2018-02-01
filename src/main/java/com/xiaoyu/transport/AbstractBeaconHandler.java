package com.xiaoyu.transport;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 继承netty的handler 用于消息的处理
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class AbstractBeaconHandler extends ChannelHandlerAdapter implements BeaconHandler {

    public AbstractBeaconHandler() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // TODO Auto-generated method stub
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void send(Object message, BaseChannel beaconChannel) throws Exception {
        if (message == null) {
            throw new Exception("发送消息为空");
        }
        beaconChannel.send(message);
    }

    @Override
    public void received(Object msg, BaseChannel beaconChannel) throws Exception {
        if (msg == null) {
            throw new Exception("接受消息为空");
        }
    }

}
