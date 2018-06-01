/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.transport;

import com.xiaoyu.transport.support.AbstractBeaconHandler;

/**
 * server端对消息的业务处理
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconServerHandler extends AbstractBeaconHandler {

    private BeaconServerChannel serverChannel;

    public BeaconServerHandler(BeaconServerChannel serverChannel) {
        this.serverChannel = serverChannel;
    }

    @Override
    public void receive(Object message) throws Exception {
        super.receive(message);
        this.serverChannel.receive(message);
    }
}
