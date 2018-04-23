package com.xiaoyu.transport;

import java.util.concurrent.Future;

import com.xiaoyu.transport.netty.NettySide;
import com.xiaoyu.transport.support.AbstractBeaconHandler;

/**
 * client断对消息的业务处理
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconClientHandler extends AbstractBeaconHandler implements NettySide {

    private BeaconClientChannel clientChannel;

    public BeaconClientHandler(BeaconClientChannel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void receive(Object message) throws Exception {
        super.receive(message);
        this.clientChannel.receive(message);
    }

    @Override
    public Future<Object> send(Object message) throws Exception {
        super.send(message);
        return this.clientChannel.sendFuture(message);
    }

}
