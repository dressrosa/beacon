package com.xiaoyu.transport;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.core.rpc.config.RpcRefer;
import com.xiaoyu.core.rpc.message.RpcRequest;
import com.xiaoyu.core.rpc.message.RpcResponse;

/**
 * server端对消息的业务处理
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconServerHandler extends BeaconHandlerAdpater {

    private static final Logger LOG = LoggerFactory.getLogger("BeaconServerHandler");

    @Override
    public void received(Object message, BaseChannel beaconChannel) throws Exception {
        RpcRequest req = (RpcRequest) message;
        RpcResponse resp = new RpcResponse();
        resp.setId(req.getId());
        if (req.isHeartbeat()) {
            beaconChannel.send(new RpcResponse().setResult("心跳回复"));
            return;
        }
        // 处理收到client信息
        Class<?> target = Class.forName(req.getInterfaceName());
        // 应该是根据信息,找到实现类.
        RpcRefer ref = target.getAnnotation(RpcRefer.class);
        Object result = null;
        try {
            for (Method d : ref.value().getDeclaredMethods()) {
                if (d.getName().equals(req.getMethodName())) {
                    result = d.invoke(ref.value().newInstance(), req.getParams());
                }
            }
        } catch (Exception e) {
            resp.setException(e);
            LOG.error("invoke method error:", e);
        }
        // 调用发送给client发送结果
        beaconChannel.send(resp.setResult(result));
        return;
    }
}
