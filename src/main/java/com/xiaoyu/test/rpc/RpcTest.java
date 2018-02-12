package com.xiaoyu.test.rpc;

import com.xiaoyu.core.rpc.context.BeaconContext;
import com.xiaoyu.test.api.IHelloService;

public class RpcTest {

    public static void main(String[] args) throws Exception {
        try {
            BeaconContext.startClient();
            IHelloService service = (IHelloService) BeaconContext.getBean(IHelloService.class);
            Object result = service.hello("好的");
            System.out.println("结果:" + result);
        } 
        catch(Exception e ) {
            e.printStackTrace();
        }
        finally {
            // System.out.println("结束2!");
            BeaconContext.stop();
        }
    }
}
