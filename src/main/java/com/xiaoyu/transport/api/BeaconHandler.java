package com.xiaoyu.transport.api;

import java.util.concurrent.Future;

/**
 * @author hongyu
 * @date 2018-02
 * @description
 */
public interface BeaconHandler {

    /**
     * @param message
     * @param beaconChannel
     * @throws Exception
     */
    public void receive(Object message) throws Exception;

    public Future<Object> send(Object message) throws Exception;

}
