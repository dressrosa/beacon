package com.xiaoyu.transport;

import java.util.concurrent.Future;

import com.xiaoyu.transport.netty.NettySide;

/**
 * client断对消息的业务处理
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconClientHandler extends BeaconHandlerAdpater implements NettySide {

    @Override
    public void received(Object msg, BaseChannel beaconChannel) throws Exception {
        super.received(msg, beaconChannel);
        beaconChannel.receive(msg);
    }

    @Override
    public Future<Object> send(Object message) throws Exception {
        BaseChannel channel = getBeaconChannel();
        return channel.send(message);
    }
}
