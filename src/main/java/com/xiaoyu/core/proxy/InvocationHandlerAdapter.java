/**
 * 
 */
package com.xiaoyu.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import com.xiaoyu.core.cluster.FaultTolerant;
import com.xiaoyu.core.common.constant.BeaconConstants;
import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.common.utils.IdUtil;
import com.xiaoyu.core.register.Registry;
import com.xiaoyu.core.rpc.config.bean.BeaconPath;
import com.xiaoyu.core.rpc.message.RpcRequest;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * @author hongyu
 * @date 2018-04
 * @description
 */
public class InvocationHandlerAdapter {

    /**
     * 在当method是父接口所属的时候;
     * method.getDeclaringClass()获取的是父接口的名称;
     * 因此这里需要持有一个接口的引用
     */
    private Class<?> ref;

    public InvocationHandlerAdapter(Class<?> ref) {
        this.ref = ref;

    }

    @SuppressWarnings("unchecked")
    public <T> T getHandler(Class<T> t) {
        if (t == InvocationHandler.class) {
            return (T) new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return preInvoke(method, args);
                }
            };
        } else if (t == MethodInterceptor.class) {
            return (T) new MethodInterceptor() {
                @Override
                public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                    return preInvoke(method, args);
                }
            };
        }
        return null;
    }

    /**
     * 封装方法信息,获取信息,返回结果
     */
    private Object preInvoke(Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        final RpcRequest req = new RpcRequest()
                .setInterfaceName(ref.getName())
                .setParams(args)
                .setMethodName(methodName);
        req.setHeartbeat(false);
        req.setId(IdUtil.requestId());
        //TODO
        if (BeaconConstants.EQUALS.equals(methodName)) {
            if (args == null || args.length == 0) {
                return false;
            }
            return ref.isInstance(args[0]);
        } else if (BeaconConstants.TO_STRING.equals(methodName)) {
            return ref.toString();
        } else if (BeaconConstants.HASHCODE.equals(methodName)) {
            return ref.hashCode();
        }
        return this.doInvoke(req);
    }

    /**
     * 检测service是否存在;获取一个client;等待请求结果;
     * 这里需要解耦
     * 
     * @param req
     * @return
     * @throws Throwable
     */
    private Object doInvoke(RpcRequest request) throws Throwable {
        Registry reg = SpiManager.defaultSpiExtender(Registry.class);
        // 判断service是否存在
        String service = request.getInterfaceName();
        boolean exist = reg.discoverService(service);
        if (!exist) {
            throw new Exception(
                    "cannot find the service->" + request.getInterfaceName() + ";please check it.");
        }
        List<BeaconPath> providers = reg.getProviders(service);
        FaultTolerant tolerant = SpiManager.defaultSpiExtender(FaultTolerant.class);

        return tolerant.invoke(request, providers);
    }
}
