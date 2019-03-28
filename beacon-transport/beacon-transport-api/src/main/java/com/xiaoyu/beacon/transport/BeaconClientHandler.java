/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.transport;

import java.util.concurrent.Future;

import com.xiaoyu.beacon.transport.support.AbstractBeaconHandler;

/**
 * client对消息的业务处理
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconClientHandler extends AbstractBeaconHandler {

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
    public Object send(Object message) throws Exception {
        super.send(message);
        return this.clientChannel.send(message);
    }

    @Override
    public Future<Object> sendFuture(Object message) throws Exception {
        super.sendFuture(message);
        return this.clientChannel.sendFuture(message);
    }

}
