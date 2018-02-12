package com.xiaoyu.transport.netty;

import java.util.concurrent.Future;

/**
 * @author hongyu
 * @date 2018-02
 * @description
 */
public interface NettySide {

    /**
     * @param message
     * @return
     * @throws Exception
     */
    public Future<Object> send(Object message) throws Exception;
}
