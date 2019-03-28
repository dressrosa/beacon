/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.spring.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

import com.xiaoyu.beacon.common.bean.ProxyWrapper;
import com.xiaoyu.beacon.common.extension.SpiManager;
import com.xiaoyu.beacon.proxy.api.IProxy;
import com.xiaoyu.beacon.registry.Registry;

/**
 * @author hongyu
 * @date 2018-04
 * @description 解决spring里面接口无法实例化的问题
 */
public class BeaconFactoryBean implements FactoryBean<Object>, DisposableBean {

    /**
     * 接口类
     */
    private Class<?> target;

    private Registry registry;

    public Class<?> getTarget() {
        return target;
    }

    public void setTarget(Class<?> target) {
        this.target = target;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public BeaconFactoryBean() {
    }

    @Override
    public void destroy() throws Exception {
        String service = target.getName();
        registry.unregisterService(registry.getLocalConsumer(service));
    }

    @Override
    public Object getObject() throws Exception {
        // 工厂bean实际返回的不是本身,而是这里的值
        return SpiManager.defaultSpiExtender(IProxy.class).getProxy(new ProxyWrapper(target));
    }

    @Override
    public Class<?> getObjectType() {
        return target;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
