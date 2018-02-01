package com.xiaoyu.test.api;

import com.xiaoyu.core.rpc.BeaconRef;

@BeaconRef(HelloServiceImpl.class)
public interface IHelloService {

    public String hello(String name);

}
