/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.core.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**2017年4月20日下午2:48:06
 * @author xiaoyu
 * @description
 */
public class CglibProxy implements IProxy {

	@Override
	public Object getProxy(final Object target) {
		Enhancer hancer = new Enhancer();
		hancer.setSuperclass(target.getClass());
		hancer.setCallback(new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				return proxy.invokeSuper(obj, args);
			}
		});
		return hancer.create();
	}

}
