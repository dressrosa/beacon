package com.xiaoyu.transport.api;

import java.util.concurrent.Future;

/**
 * @author hongyu
 * @date 2018-04
 * @description
 */
public interface Client {

    public Future<Object> send(Object message);

    public void stop();

    public void start();
}
