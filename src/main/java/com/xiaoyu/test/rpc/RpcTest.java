package com.xiaoyu.test.rpc;

import com.xiaoyu.core.rpc.context.BeaconContext;
import com.xiaoyu.test.api.IHelloService;

public class RpcTest {
    public static void main(String[] args) throws Exception {
        try {
            BeaconContext.start();
            for(int i =0;i< 50; i++) {
                IHelloService service = (IHelloService) BeaconContext
                        .getProxyMap()
                        .get(IHelloService.class);
                System.out.println("答案:" + service.hello("tom"+i));
            }
        } finally {
            BeaconContext.stop();
        }

    }
}
