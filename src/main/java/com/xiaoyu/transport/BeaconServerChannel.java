package com.xiaoyu.transport;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.core.rpc.message.RpcRequest;
import com.xiaoyu.core.rpc.message.RpcResponse;
import com.xiaoyu.transport.api.BaseChannel;
import com.xiaoyu.transport.support.AbstractBeaconChannel;

/**
 * 服务端channel
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconServerChannel extends AbstractBeaconChannel {

    private static final Logger LOG = LoggerFactory.getLogger(BeaconServerChannel.class);

    protected BaseChannel baseChannel;

    public BeaconServerChannel(BaseChannel baseChannel) {
        this.baseChannel = baseChannel;
    }

    @Override
    protected Object doSend(Object message) {
        try {
            /*
             * this.channel
             * .pipeline()
             * .context("beaconServerHandler")
             * .writeAndFlush(message);
             */
            this.baseChannel.send(message);
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    protected void doReceive(Object message) throws Exception {
        RpcResponse resp = new RpcResponse();
        Object result = null;
        try {
            RpcRequest req = (RpcRequest) message;
            resp.setId(req.getId());
            if (req.isHeartbeat()) {
                this.baseChannel.send(new RpcResponse().setResult("pong"));
                return;
            }
            // 处理收到client信息
            Class<?> target = Class.forName(req.getInterfaceImpl());
            // 应该是根据信息,找到实现类

            for (Method d : target.getDeclaredMethods()) {
                if (d.getName().equals(req.getMethodName())) {
                    result = d.invoke(target.newInstance(), req.getParams());
                }
            }
        } catch (Exception e) {
            LOG.error("error->" + e);
            resp.setException(e);
        }
        // 调用发送给client发送结果
        this.baseChannel.send(resp.setResult(result));
    }

    @Override
    protected Future<Object> doSendFuture(Object message) {
        try {
            this.baseChannel.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return null;
    }

}
