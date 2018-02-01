package com.xiaoyu.test.netty;

import com.xiaoyu.transport.netty.NettyServer;

public class NettyServerTest {

    public static void main(String args[]) throws Exception {
        System.out.println("server start");
        new NettyServer(9090).bind();
    }
}
