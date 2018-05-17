/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.xiaoyu.core.rpc.api.IProxy;

/**
 * @author hongyu
 * @date 2017-04
 * @description
 */
public class JdkProxy implements IProxy {

    private InvocationHandler invocationHandler;

    @Override
    public Object getProxy(final Object target) {
        Class<?> cls = null;
        // 接口提供给client
        if (target instanceof Class && (cls = (Class<?>) target).isInterface()) {
            invocationHandler = new InvocationHandlerAdapter(cls).getHandler(InvocationHandler.class);
            return Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[] { cls },
                    invocationHandler);
        } else {
            // server直接调用实现类
            cls = target.getClass();
            invocationHandler = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return method.invoke(target, args);
                }
            };
            return Proxy.newProxyInstance(cls.getClassLoader(), cls.getInterfaces(),
                    invocationHandler);
        }

    }

}
