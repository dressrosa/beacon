/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 2017年4月20日下午2:41:42
 * 
 * @author xiaoyu
 * @description
 */
public class JdkProxy implements IProxy {

	@Override
	public Object getProxy(final Object target) {
		return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(),
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						return method.invoke(target, args);
					}
				});
	}

}
