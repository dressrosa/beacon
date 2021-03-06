/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.transport;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.beacon.common.constant.BeaconConstants;
import com.xiaoyu.beacon.common.exception.BizException;
import com.xiaoyu.beacon.common.extension.SpiManager;
import com.xiaoyu.beacon.common.message.RpcRequest;
import com.xiaoyu.beacon.common.message.RpcResponse;
import com.xiaoyu.beacon.registry.Registry;
import com.xiaoyu.beacon.transport.api.BaseChannel;
import com.xiaoyu.beacon.transport.support.AbstractBeaconChannel;

/**
 * 服务端channel
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconServerChannel extends AbstractBeaconChannel {

    private static final Logger LOG = LoggerFactory.getLogger(BeaconServerChannel.class);

    /**
     * lru+弱引用 缓存最近的class.forname反射出的class
     */
    private static final LinkedHashMap<String, WeakReference<Class<?>>> Class_Cache = new LinkedHashMap<String, WeakReference<Class<?>>>() {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Entry<String, WeakReference<Class<?>>> eldest) {
            return this.size() > 8;
        }

    };
    /**
     * 具体的发送channel,比如nettychannel和httpchannel
     */
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
                    LOG.error("" + e);
                }
            }
        });
        return null;
    }

    @Override
    protected void doReceive(Object message) throws Exception {
        // 同一个channel下会导致处理同步,这里通过线程池将io与业务分隔开.
        this.addTask(() -> {
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
                            Class<?> target = getLocalClass(req.getInterfaceImpl());
                            result = target.newInstance().toString();
                        }
                    } else if (BeaconConstants.HASHCODE.equals(req.getMethodName())) {
                        // 有spring的bean
                        if (proxy != null) {
                            result = proxy.hashCode();
                        } else {
                            Class<?> target = getLocalClass(req.getInterfaceImpl());
                            result = target.newInstance().hashCode();
                        }
                    } else {
                        // 根据信息,找到实现类 declareMethods是不包含toString,hashCode的
                        if (proxy != null) {
                            Class<?> cl1 = proxy.getClass();
                            Method d = null;
                            // spring java原生代理
                            if (cl1.getName().equals(req.getInterfaceImpl())) {
                                d = cl1.getMethod(req.getMethodName(), req.getParamTypes());
                            } else {
                                // spring cglib代理
                                d = cl1.getSuperclass().getMethod(req.getMethodName(), req.getParamTypes());
                            }
                            if (d.getReturnType() != (Class<?>) req.getReturnType()) {
                                throw new Exception("Cannot find method with the retrunType->"
                                        + ((Class<?>) req.getReturnType()).getName());
                            }
                            result = d.invoke(proxy, req.getParams());
                        } else {
                            Class<?> target = getLocalClass(req.getInterfaceImpl());
                            Method d = target.getMethod(req.getMethodName(), req.getParamTypes());
                            if (d.getReturnType().getTypeName().equals("void")
                                    && ((Class<?>) req.getReturnType()).getTypeName().equals("java.lang.Void")) {
                            } else if (!d.getReturnType().getName()
                                    .equals(((Class<?>) req.getReturnType()).getName())) {
                                throw new Exception(
                                        "Cannot find method " + req.getMethodName() + " with the retrunType->"
                                                + ((Class<?>) req.getReturnType()).getName());
                            }
                            result = d.invoke(target.newInstance(), req.getParams());
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
        });
    }

    private final Class<?> getLocalClass(String interfaceName) throws ClassNotFoundException {
        Class<?> cls = null;
        if (Class_Cache.containsKey(interfaceName)) {
            cls = Class_Cache.get(interfaceName).get();
        }
        if (cls == null) {
            cls = Class.forName(interfaceName);
        }
        return cls;
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
