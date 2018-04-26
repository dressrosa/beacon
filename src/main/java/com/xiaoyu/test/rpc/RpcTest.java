package com.xiaoyu.test.rpc;

import com.xiaoyu.core.rpc.context.BeaconTestContext;
import com.xiaoyu.test.api.IHelloService;

public class RpcTest {

    public static void main(String[] args) throws Exception {
        try {
            BeaconTestContext.startClient();
            IHelloService service = (IHelloService) BeaconTestContext.getBean(IHelloService.class);
            Object result = service.hello("好的");
            System.out.println("结果:" + result);
        } 
        catch(Exception e ) {
            e.printStackTrace();
        }
        finally {
            // System.out.println("结束2!");
            BeaconTestContext.stop();
        }
    }
}
