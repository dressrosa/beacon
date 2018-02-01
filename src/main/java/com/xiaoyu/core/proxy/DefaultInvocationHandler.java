package com.xiaoyu.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

import com.xiaoyu.core.rpc.message.RpcMessage;
import com.xiaoyu.core.rpc.message.RpcRequest;
import com.xiaoyu.core.rpc.message.RpcResponse;
import com.xiaoyu.transport.BeaconClientChannel;

/**
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class DefaultInvocationHandler implements InvocationHandler {

    /**
     *TODO 封装方法信息,,获取server端,选择一个server,放入netty,获取信息,返回结果 
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?> ref = method.getDeclaringClass();

        RpcMessage req = new RpcRequest()
                .setInterfaceName(ref.getName())
                .setParams(args)
                .setMethodName(methodName)
                .setHeartbeat(false);
        if ("equals".equals(methodName)) {
            return false;
        } else if ("toString".equals(methodName)) {
            return method.toString();
        } else if ("hashCode".equals(methodName)) {
            return proxy.hashCode();
        }

        return this.doInvoke(req);
    }

    private Object doInvoke(RpcMessage req) throws Exception {
        // 获取channel发送消息,返回future
        Future<Object> future = BeaconClientChannel.getChannel().send(req);
        RpcResponse result = (RpcResponse) future.get();
        return result.getResult();
    }

}
