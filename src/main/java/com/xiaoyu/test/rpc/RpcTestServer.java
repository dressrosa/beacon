package com.xiaoyu.test.rpc;

import com.xiaoyu.core.rpc.context.BeaconContext;

public class RpcTestServer {

    public static void main(String[] args) throws Exception {
        try {
            BeaconContext.startServer();
        } finally {
            System.out.println("结束2!");
           // BeaconContext.stop();
        }
    }
}
