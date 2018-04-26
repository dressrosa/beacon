package com.xiaoyu.test.api;

import com.xiaoyu.core.rpc.config.anno.RpcRefer;

@RpcRefer(HelloServiceImpl.class)
public interface IHelloService extends IBaseService{

    public String hello(String name);
    
    public void sing(String song);

}
