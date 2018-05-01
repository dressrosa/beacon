package com.xiaoyu.test.rpc;

import com.xiaoyu.core.rpc.context.BeaconTestContext;
import com.xiaoyu.core.rpc.context.Context;

public class RpcTestServer {

    public static void main(String[] args) throws Exception {
        try {
            BeaconTestContext
                    .startServer();
        } finally {
            System.out.println("结束2!");
            // BeaconTestContext.stop();
        }
    }
}
