package com.xiaoyu.spring.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.proxy.IProxy;

/**
 * 解决spring里面接口无法实例化的问题
 * 
 * @author hongyu
 * @date 2018-04
 * @description
 */
public class BeaconFactoryBean implements FactoryBean<Object>, InitializingBean, DisposableBean {

    private Class<?> cls;

    public BeaconFactoryBean(Class<?> cls) {
        this.cls = cls;
    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void afterPropertiesSet() throws Exception {

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
