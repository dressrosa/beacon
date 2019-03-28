/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.transport.support;

import java.util.concurrent.Future;

import com.xiaoyu.beacon.transport.api.BeaconHandler;

/**
 * @author hongyu
 * @date 2018-02
 * @description 用于对消息的业务处理
 */
public class AbstractBeaconHandler implements BeaconHandler {

    @Override
    public void receive(Object message) throws Exception {
        if (message == null) {
            throw new Exception("Message received is null");
        }
    }

    @Override
    public Object send(Object message) throws Exception {
        if (message == null) {
            throw new Exception("Message be sent is null.");
        }
        return null;
    }

    @Override
    public Future<Object> sendFuture(Object message) throws Exception {
        if (message == null) {
            throw new Exception("Message be sent is null.");
        }
        return null;
    }

}
