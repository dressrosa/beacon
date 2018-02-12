package com.xiaoyu.transport;

import java.util.concurrent.Future;

import io.netty.channel.Channel;

/**
 * 服务端channel
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconServerChannel extends AbstractBeaconChannel {

    public BeaconServerChannel(Channel ch, BeaconHandler beaconHandler) {
        super(ch, beaconHandler);
    }

    @Override
    protected Future<Object> doSend(Object message) {
        try {
            this.channel
                   // .pipeline()
                   // .context("beaconServerHandler")
                    .writeAndFlush(message);
        } finally {

        }
        return null;
    }

    @Override
    protected void doReceive(Object message) {
        this.channel.pipeline().context("beaconServerHandler").read();
    }

}
