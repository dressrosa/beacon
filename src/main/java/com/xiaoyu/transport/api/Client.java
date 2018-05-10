package com.xiaoyu.transport.api;

import java.util.concurrent.Future;

/**
 * @author hongyu
 * @date 2018-04
 * @description
 */
public interface Client extends BeaconSide {

    public Future<Object> sendFuture(Object message) throws Exception;

    public Object send(Object message) throws Exception;

}
