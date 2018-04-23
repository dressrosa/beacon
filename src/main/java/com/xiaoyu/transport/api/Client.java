package com.xiaoyu.transport.api;

import java.util.concurrent.Future;

public interface Client {

    public Future<Object> send(Object message);
    
    public void stop();

    public void start();
}
