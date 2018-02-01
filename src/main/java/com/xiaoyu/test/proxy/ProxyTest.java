package com.xiaoyu.test.proxy;

import com.xiaoyu.core.proxy.DefaultProxy;
import com.xiaoyu.test.api.HelloServiceImpl;
import com.xiaoyu.test.api.IHelloService;

public class ProxyTest {

    public static void main(String[] args) {
        IHelloService hello = (IHelloService) DefaultProxy.getProxy(new HelloServiceImpl());
        System.out.println(hello.hello("xiao"));
    }
}
