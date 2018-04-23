package com.xiaoyu.transport;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.core.rpc.config.RpcRefer;
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
            // this.channel
            // // .pipeline()
            // // .context("beaconServerHandler")
            // .writeAndFlush(message);
            this.baseChannel.send(message);
        } catch (Exception e) {
            
        } finally {

        }
        return null;
    }

    @Override
    protected void doReceive(Object message) {
        // this.channel.pipeline().context("beaconServerHandler").read();
        try {
            RpcRequest req = (RpcRequest) message;
            RpcResponse resp = new RpcResponse();
            resp.setId(req.getId());
            if (req.isHeartbeat()) {
                this.baseChannel.send(new RpcResponse().setResult("心跳回复"));
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
            }
            // 调用发送给client发送结果
            this.baseChannel.send(resp.setResult(result));
        } catch (Exception e) {
            LOG.error("error->"+e);
        }

    }

    @Override
    protected Future<Object> doSendFuture(Object message) {
        try {
            // this.channel
            // // .pipeline()
            // // .context("beaconServerHandler")
            // .writeAndFlush(message);
            this.baseChannel.send(message);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

        }
        return null;
    }

}
