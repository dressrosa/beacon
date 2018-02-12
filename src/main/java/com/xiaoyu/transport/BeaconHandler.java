package com.xiaoyu.transport;

import io.netty.channel.ChannelHandler;

/**
 * @author hongyu
 * @date 2018-02
 * @description
 */
public interface BeaconHandler extends ChannelHandler {

    /**
     * @param message
     * @param beaconChannel
     * @throws Exception
     */
    public void received(Object message, BaseChannel beaconChannel) throws Exception;

    /**
     * 
     */
    public void stop();
}
