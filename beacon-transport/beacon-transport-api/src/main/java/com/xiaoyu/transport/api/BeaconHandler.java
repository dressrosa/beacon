/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.transport.api;

import java.util.concurrent.Future;

/**
 * @author hongyu
 * @date 2018-02
 * @description 基本的接收/发送处理
 */
public interface BeaconHandler {

    /**
     * @param message
     * @param beaconChannel
     * @throws Exception
     */
    public void receive(Object message) throws Exception;

    public Future<Object> sendFuture(Object message) throws Exception;

    public Object send(Object message) throws Exception;

}
