/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.transport.api;

import java.util.concurrent.Future;

/**
 * @author hongyu
 * @date 2018-02
 * @description 基础的接收/发送
 */
public interface BaseChannel {

    /**
     * @param message
     * @return
     * @throws Exception
     */
    public Future<Object> sendFuture(Object message) throws Exception;

    public Object send(Object message) throws Exception;

    /**
     * @param msg
     * @throws Exception
     */
    public void receive(Object msg) throws Exception;
}
