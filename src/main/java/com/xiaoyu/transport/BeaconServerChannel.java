package com.xiaoyu.transport;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * 服务端channel
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconServerChannel extends AbstractBeaconChannel {

    private static final Logger LOG = LoggerFactory.getLogger("BeaconServerChannel");

    public BeaconServerChannel(Channel ch) {
        super(ch);
    }

    public static BeaconServerChannel getChannel(Channel ch) {
        BaseChannel beaconCh = CHANNEL_MAP.get(ch);
        if (beaconCh == null) {
            CHANNEL_MAP.putIfAbsent(ch, (beaconCh = new BeaconServerChannel(ch)));
        }
        LOG.info("map Size:{}", CHANNEL_MAP.size());
        return (BeaconServerChannel) beaconCh;
    }

    @Override
    public Future<Object> send(Object message) throws Exception {
        super.send(message);
        try {
            ChannelFuture future = this.channel.pipeline().context("beaconServerHandler").writeAndFlush(message);
            future.get();
        } finally {

        }
        return null;
    }

    @Override
    public void receive(Object message) throws Exception {
        super.receive(message);
        this.channel.pipeline().context("beaconServerHandler").read();
    }

    public void removeChannel(Channel channel) {
        CHANNEL_MAP.remove(channel);
    }

}
