package com.xiaoyu.test.rpc;

import com.xiaoyu.core.rpc.context.BeaconTestContext;

public class RpcTestServer {

    public static void main(String[] args) throws Exception {
        try {
            BeaconTestContext.startServer();
            BeaconTestContext.startClient();
        } finally {
            System.out.println("结束2!");
           // BeaconTestContext.stop();
        }
    }
}
