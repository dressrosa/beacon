package com.xiaoyu.transport;

import java.lang.reflect.Method;

import com.xiaoyu.core.rpc.BeaconRef;
import com.xiaoyu.core.rpc.message.RpcRequest;
import com.xiaoyu.core.rpc.message.RpcResponse;

/**
 * server端对消息的业务处理
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconServerHandler extends AbstractBeaconHandler {

    public BeaconServerHandler() {
    }

    @Override
    public void received(Object msg, BaseChannel beaconChannel) throws Exception {
        RpcRequest req = (RpcRequest) msg;
        if (req.isHeartbeat()) {
            this.send(new RpcResponse().setResult("心跳回复"), beaconChannel);
            return;
        }
        // 处理收到client信息
        Class<?> target = Class.forName(req.getInterfaceName());
        // 应该是根据的信息,找到实现类.
        BeaconRef ref = target.getAnnotation(BeaconRef.class);
        Object result = null;
        for (Method d : ref.value().getDeclaredMethods()) {
            if (d.getName().equals(req.getMethodName())) {
                result = d.invoke(ref.value().newInstance(), req.getParams());
            }
        }
        this.send(new RpcResponse().setResult(result), beaconChannel);
        return;
    }

}
