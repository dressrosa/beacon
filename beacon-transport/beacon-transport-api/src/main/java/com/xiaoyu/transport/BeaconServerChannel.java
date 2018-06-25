/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.transport;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.core.common.constant.BeaconConstants;
import com.xiaoyu.core.common.exception.BizException;
import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.common.message.RpcRequest;
import com.xiaoyu.core.common.message.RpcResponse;
import com.xiaoyu.core.register.Registry;
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
        this.addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    /*
                     * this.channel
                     * .pipeline()
                     * .context("beaconServerHandler")
                     * .writeAndFlush(message);
                     */
                    baseChannel.send(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return null;
    }

    @Override
    protected void doReceive(Object message) throws Exception {
        // 同一个channel下会导致处理同步,这里通过线程池将io与业务分隔开.
        this.addTask(new Runnable() {
            @Override
            public void run() {
                RpcResponse resp = new RpcResponse();
                Object result = null;
                try {
                    RpcRequest req = (RpcRequest) message;
                    resp.setId(req.getId());
                    if (req.isHeartbeat()) {
                        baseChannel.send(new RpcResponse().setResult("pong"));
                        return;
                    }
                    try {
                        // 处理收到client信息
                        Class<?> target = Class.forName(req.getInterfaceImpl());
                        if (BeaconConstants.TO_STRING.equals(req.getMethodName())) {
                            Registry registry = SpiManager.defaultSpiExtender(Registry.class);
                            Object proxy = registry.getProxyBean(req.getInterfaceName());
                            // 有spring的bean
                            if (proxy != null) {
                                result = proxy.toString();
                            } else {
                                result = target.newInstance().toString();
                            }

                        } else if (BeaconConstants.HASHCODE.equals(req.getMethodName())) {
                            Registry registry = SpiManager.defaultSpiExtender(Registry.class);
                            Object proxy = registry.getProxyBean(req.getInterfaceName());
                            // 有spring的bean
                            if (proxy != null) {
                                result = proxy.hashCode();
                            } else {
                                result = target.newInstance().hashCode();
                            }

                        } else {
                            // 根据信息,找到实现类
                            for (Method d : target.getDeclaredMethods()) {
                                if (d.getName().equals(req.getMethodName())) {
                                    Registry registry = SpiManager.defaultSpiExtender(Registry.class);
                                    Object proxy = registry.getProxyBean(req.getInterfaceName());
                                    // 有spring的bean
                                    if (proxy != null) {
                                        result = d.invoke(proxy, req.getParams());
                                    } else {
                                        result = d.invoke(target.newInstance(), req.getParams());
                                    }
                                    break;
                                }
                            }
                        }
                    } catch (Exception bize) {
                        // 非rpc异常
                        throw new BizException(bize);
                    }
                    // 调用发送给client发送结果
                    baseChannel.send(resp.setResult(result));
                } catch (Exception e) {
                    LOG.error("error->" + e);
                    resp.setException(e);
                }
            }
        });

    }

    @Override
    protected Future<Object> doSendFuture(Object message) {
        try {
            this.baseChannel.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
