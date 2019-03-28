/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.beacon.transport.http;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import com.xiaoyu.beacon.common.constant.From;
import com.xiaoyu.beacon.common.message.RpcRequest;
import com.xiaoyu.beacon.common.message.RpcResponse;
import com.xiaoyu.beacon.transport.BeaconClientChannel;
import com.xiaoyu.beacon.transport.BeaconClientHandler;
import com.xiaoyu.beacon.transport.BeaconServerChannel;
import com.xiaoyu.beacon.transport.BeaconServerHandler;
import com.xiaoyu.beacon.transport.api.BaseChannel;
import com.xiaoyu.beacon.transport.api.BeaconHandler;

import feign.Feign.Builder;

/**
 * @author hongyu
 * @date 2018-08
 * @description
 */
public class HttpChannel implements BaseChannel {

    public static final ConcurrentMap<Builder, BeaconHandler> CHANNEL_MAP = new ConcurrentHashMap<>(16);

    /**
     * feign builder
     */
    private Builder client;

    /**
     * server host
     */
    private String url;

    public HttpChannel(Builder client, String url) {
        this.client = client;
        this.url = url;
    }

    public static BeaconHandler getChannel(Builder client, String url, From side) throws Exception {
        BeaconHandler beacon = CHANNEL_MAP.get(client);
        if (beacon == null) {
            HttpChannel nc = new HttpChannel(client, url);
            if (From.CLIENT == side) {
                BeaconHandler b = new BeaconClientHandler(new BeaconClientChannel(nc));
                CHANNEL_MAP.putIfAbsent(client, (beacon = b));
            } else {
                BeaconHandler b = new BeaconServerHandler(new BeaconServerChannel(nc));
                CHANNEL_MAP.putIfAbsent(client, (beacon = b));
            }
        }
        return beacon;
    }

    @Override
    public Future<Object> sendFuture(Object message) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object send(Object message) throws Exception {
        RpcRequest req = (RpcRequest) message;
        Class<?> cls = (Class<?>) Class.forName(req.getInterfaceName());
        Object result = cls.getMethod(req.getMethodName(), req.getParamTypes())
                .invoke(client.target(cls, url), req.getParams());
        RpcResponse resp = new RpcResponse()
                .setResult(result);
        resp.setId(req.getId());
        try {
            // netty是异步接收,这里直接触发
            this.receive(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void receive(Object msg) throws Exception {
        CHANNEL_MAP.get(client).receive(msg);
    }

}
