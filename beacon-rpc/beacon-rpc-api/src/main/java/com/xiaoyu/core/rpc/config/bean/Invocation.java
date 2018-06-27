/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.core.rpc.config.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.core.common.bean.BeaconPath;
import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.common.message.RpcRequest;
import com.xiaoyu.core.common.message.RpcResponse;
import com.xiaoyu.core.rpc.api.Context;

/**
 * 调用封装
 * 
 * @author hongyu
 * @date 2018-05
 * @description
 */
public class Invocation {

    private static final Logger LOG = LoggerFactory.getLogger(Invocation.class);

    private BeaconPath consumer;

    private RpcRequest request;

    public Invocation(BeaconPath consumer, RpcRequest request) {
        this.consumer = consumer;
        this.request = request;
    }

    public BeaconPath getConsumer() {
        return consumer;
    }

    public RpcRequest getRequest() {
        return request;
    }

    public Object invoke(BeaconPath provider) throws Throwable {
        request.setInterfaceImpl(provider.getRef());
        request.setTimeout(Long.valueOf(consumer.getTimeout()));
        // 发送消息
        Object ret = SpiManager.defaultSpiExtender(Context.class)
                .client(provider.getHost(), Integer.valueOf(provider.getPort()))
                .send(request);
        RpcResponse result = (RpcResponse) ret;
        if (result.getException() != null) {
            LOG.error("Beacon exception->", result.getException());
            throw result.getException();
        }
        return result.getResult();
    }

}
