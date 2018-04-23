package com.xiaoyu.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

import com.alibaba.fastjson.JSON;
import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.common.utils.IdUtil;
import com.xiaoyu.core.register.Registry;
import com.xiaoyu.core.rpc.context.BeaconContext;
import com.xiaoyu.core.rpc.message.RpcMessage;
import com.xiaoyu.core.rpc.message.RpcRequest;
import com.xiaoyu.core.rpc.message.RpcResponse;

/**
 * 执行数据的传输
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class DefaultInvocationHandler implements InvocationHandler {

    /**
     * 在当method是父接口所属的时候;
     * method.getDeclaringClass()获取的是父接口的名称;
     * 因此这里需要持有一个接口的引用
     */
    private Class<?> ref;

    public DefaultInvocationHandler(Class<?> ref) {
        this.ref = ref;
    }

    /**
     * 封装方法信息,获取信息,返回结果
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        final RpcMessage req = new RpcRequest()
                .setInterfaceName(ref.getName())
                .setParams(args)
                .setMethodName(methodName)
                .setHeartbeat(false)
                .setId(IdUtil.requestId());
        if ("equals".equals(methodName)) {
            if (args == null || args.length == 0) {
                return false;
            }
            return ref.isInstance(args[0]);
        } else if ("toString".equals(methodName)) {
            return ref.toString();
        } else if ("hashCode".equals(methodName)) {
            return ref.hashCode();
        }
        return this.doInvoke(req);
    }

    /**
     * 检测service是否存在;获取一个client;等待请求结果;
     * 
     * @param req
     * @return
     * @throws Throwable
     */
    private Object doInvoke(RpcMessage req) throws Throwable {
        Registry reg = SpiManager.defaultSpiExtender(Registry.class);
        // 判断service是否存在
        boolean exist = reg.discoverService(((RpcRequest) req).getInterfaceName());
        if (!exist) {
            throw new Exception("not find the service->" + ((RpcRequest) req).getInterfaceName() + "please check it.");
        }
        // 获取channel发送消息,返回future
        Future<Object> future = BeaconContext.client().send(req);
        RpcResponse result = (RpcResponse) future.get();
        System.out.println("返回json:" + JSON.toJSONString(result));
        if (result.getException() != null) {
            throw result.getException();
        }
        return result.getResult();
    }

}
