package com.xiaoyu.test.proxy;

import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.proxy.IProxy;
import com.xiaoyu.test.api.IHelloService;

public class ProxyTest {

    public static void main(String[] args) throws Exception {
        IHelloService hello = (IHelloService) SpiManager.defaultSpiExtender(IProxy.class)
                .getProxy(IHelloService.class);
        System.out.println(hello.hello("xiao"));
    }
}
