package com.xiaoyu.transport.support;

import java.util.concurrent.Future;

import com.xiaoyu.transport.api.BeaconHandler;

/**
 * 用于消息的处理
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class AbstractBeaconHandler implements BeaconHandler {

    @Override
    public void receive(Object message) throws Exception {
        if (message == null) {
            throw new Exception("接受消息为空");
        }
    }

    @Override
    public Future<Object> send(Object message) throws Exception {
        if (message == null) {
            throw new Exception("发送消息为空");
        }
        return null;
    }

}
