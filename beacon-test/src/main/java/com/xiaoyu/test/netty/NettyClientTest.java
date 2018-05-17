package com.xiaoyu.test.netty;

import com.xiaoyu.transport.netty.NettyClient;

public class NettyClientTest {

    public static void main(String args[]) throws Exception {

        NettyClient client = new NettyClient("127.0.0.1", 9090);
        System.out.println("client start");
        client.start();
    }
}
