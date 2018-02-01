package com.xiaoyu.transport;

/**
 * client断对消息的业务处理
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconClientHandler extends AbstractBeaconHandler {

    public BeaconClientHandler() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void received(Object msg, BaseChannel beaconChannel) throws Exception {
        super.received(msg, beaconChannel);
        beaconChannel.receive(msg);
    }

}
