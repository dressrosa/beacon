package com.xiaoyu.spring.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.xiaoyu.core.common.bean.BeaconPath;
import com.xiaoyu.core.common.constant.From;
import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.common.utils.NetUtil;
import com.xiaoyu.core.register.Registry;
import com.xiaoyu.core.rpc.api.IProxy;

/**
 * 解决spring里面接口无法实例化的问题
 * 
 * @author hongyu
 * @date 2018-04
 * @description
 */
public class BeaconFactoryBean implements FactoryBean<Object>, InitializingBean, DisposableBean {

    private Class<?> cls;

    private Registry registry;

    private From side;

    public BeaconFactoryBean(Class<?> cls, Registry registry, From side) {
        this.cls = cls;
        this.registry = registry;
        this.side = side;
    }

    @Override
    public void destroy() throws Exception {
        String service = cls.getName();
        BeaconPath path = new BeaconPath();
        path.setHost(NetUtil.localIP())
                .setSide(side)
                .setService(service);
        registry.unregisterService(path);
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
