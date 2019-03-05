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
                        Registry registry = SpiManager.defaultSpiExtender(Registry.class);
                        Object proxy = registry.getProxyBean(req.getInterfaceName());
                        if (BeaconConstants.TO_STRING.equals(req.getMethodName())) {
                            // 有spring的bean
                            if (proxy != null) {
                                result = proxy.toString();
                            } else {
                                Class<?> target = Class.forName(req.getInterfaceImpl());
                                result = target.newInstance().toString();
                            }
                        } else if (BeaconConstants.HASHCODE.equals(req.getMethodName())) {
                            // 有spring的bean
                            if (proxy != null) {
                                result = proxy.hashCode();
                            } else {
                                Class<?> target = Class.forName(req.getInterfaceImpl());
                                result = target.newInstance().hashCode();
                            }
                        } else {
                            // 根据信息,找到实现类 declareMethods是不包含toString,hashCode的
                            if (proxy != null) {
                                Class<?> cl1 = proxy.getClass();
                                Method[] methods = null;
                                // spring java原生代理
                                if (cl1.getName().equals(req.getInterfaceImpl())) {
                                    methods = cl1.getDeclaredMethods();
                                } else {
                                    // spring cglib代理
                                    methods = cl1.getSuperclass().getDeclaredMethods();
                                }
                                for (Method d : methods) {
                                    if (d.getName().equals(req.getMethodName())) {
                                        result = d.invoke(proxy, req.getParams());
                                        break;
                                    }
                                }
                            } else {
                                Class<?> target = Class.forName(req.getInterfaceImpl());
                                Method[] methods = target.getDeclaredMethods();
                                for (Method d : methods) {
                                    if (d.getName().equals(req.getMethodName())) {
                                        result = d.invoke(target.newInstance(), req.getParams());
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Exception bize) {
                        // 非rpc异常
                        throw new BizException(bize);
                    }
                } catch (Throwable e) {
                    resp.setException(e);
                    LOG.error("Beacon exception->", e);
                }
                // 调用发送给client发送结果
                try {
                    baseChannel.send(resp.setResult(result));
                } catch (Exception e) {
                    LOG.error("Beacon exception->", e);
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
