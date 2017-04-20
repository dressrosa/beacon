/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.core.proxy;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author:xiaoyu 2017年3月21日下午10:28:16
 *
 * @description:默认的代理实现
 */
public class DefaultProxy {

	private static IProxy proxy;

	public static Object getProxy(Object target) {
		ServiceLoader<IProxy> loader = ServiceLoader.load(IProxy.class);
		Iterator<IProxy> iter = loader.iterator();
		while (proxy == null && iter.hasNext())
			proxy = iter.next();
		if (proxy == null)
			proxy = new JdkProxy();
		return proxy.getProxy(target);
	}

}
