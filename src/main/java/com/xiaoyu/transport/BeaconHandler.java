package com.xiaoyu.transport;

import io.netty.channel.ChannelHandler;

/**
 * @author hongyu
 * @date 2018-02
 * @description
 */
public interface BeaconHandler extends ChannelHandler {

    public void send(Object message, BaseChannel beaconChannel) throws Exception;

    public void received(Object msg, BaseChannel beaconChannel) throws Exception;
}
