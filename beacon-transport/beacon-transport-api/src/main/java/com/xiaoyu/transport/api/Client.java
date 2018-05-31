/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.transport.api;

import java.util.concurrent.Future;

/**
 * @author hongyu
 * @date 2018-04
 * @description
 */
public interface Client extends BeaconSide {

    /**
     * 异步发送
     * 
     * @param message
     * @return
     * @throws Exception
     */
    public Future<Object> sendFuture(Object message) throws Exception;

    /**
     * 同步发送
     * 
     * @param message
     * @return
     * @throws Exception
     */
    public Object send(Object message) throws Exception;

}
