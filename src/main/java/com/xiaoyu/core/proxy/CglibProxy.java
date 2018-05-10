/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.core.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * @author hongyu
 * @date 2017-04
 * @description
 */
public class CglibProxy implements IProxy {

    @Override
    public Object getProxy(final Object target) {
        final Enhancer hancer = new Enhancer();
        Class<?> cls = null;
        // 接口提供给client
        if (target instanceof Class && (cls = (Class<?>) target).isInterface()) {
            hancer.setSuperclass(cls);
            hancer.setCallback(new InvocationHandlerAdapter(cls).getHandler(MethodInterceptor.class));
            return hancer.create();
        } else {
            // server直接调用实现类
            cls = target.getClass();
            hancer.setSuperclass(cls);
            hancer.setCallback(new MethodInterceptor() {
                @Override
                public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                    return proxy.invokeSuper(obj, args);
                }
            });
            return hancer.create();
        }
    }
}
