/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.beacon.proxy.cglib;

import java.lang.reflect.Method;

import com.xiaoyu.beacon.common.bean.ProxyWrapper;
import com.xiaoyu.beacon.proxy.api.IProxy;
import com.xiaoyu.beacon.proxy.api.InvocationHandlerAdapter;

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
    public Object getProxy(ProxyWrapper wrapper) {
        final Enhancer hancer = new Enhancer();
        Class<?> cls = null;
        Object target = wrapper.getTarget();
        // 接口提供给client
        if (target instanceof Class && (cls = (Class<?>) target).isInterface()) {
            hancer.setSuperclass(cls);
            hancer.setCallback(new InvocationHandlerAdapter(wrapper).getHandler(MethodInterceptor.class));
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
