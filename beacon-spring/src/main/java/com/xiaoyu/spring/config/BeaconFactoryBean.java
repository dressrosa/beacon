/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.spring.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.register.Registry;
import com.xiaoyu.core.rpc.api.IProxy;

/**
 * @author hongyu
 * @date 2018-04
 * @description 解决spring里面接口无法实例化的问题
 */
public class BeaconFactoryBean implements FactoryBean<Object>, DisposableBean {

    /**
     * 接口类
     */
    private Class<?> cls;

    private Registry registry;

    public BeaconFactoryBean(Class<?> cls, Registry registry) {
        this.cls = cls;
        this.registry = registry;
    }

    @Override
    public void destroy() throws Exception {
        String service = cls.getName();
        registry.unregisterService(registry.getLocalConsumer(service));
    }

    @Override
    public Object getObject() throws Exception {
        // 工厂bean实际返回的不是本身,而是这里的值
        return SpiManager.defaultSpiExtender(IProxy.class).getProxy(cls);
    }

    @Override
    public Class<?> getObjectType() {
        return cls;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
