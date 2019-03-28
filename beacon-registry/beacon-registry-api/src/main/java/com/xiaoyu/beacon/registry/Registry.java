/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.registry;

import java.util.List;

import com.xiaoyu.beacon.common.bean.BeaconPath;

/**
 * @author hongyu
 * @date 2018-02
 * @description
 */
public interface Registry {

    /**
     * 注册地址
     * 
     * @param addr
     */
    public void address(String addr);

    /**
     * 关闭注册中心
     */
    public void close();

    /**
     * 是否连接
     * 
     * @param addr
     * @return
     */
    public boolean isInit();

    /**
     * 发现服务
     * 
     * @param service
     * @return
     */
    public boolean discoverService(String service);

    /**
     * 注册服务
     * 
     * @param beaconPath
     */
    public void registerService(BeaconPath beaconPath);

    /**
     * 取消注册服务
     * 
     * @param beaconPath
     */
    public void unregisterService(BeaconPath beaconPath);

    /**
     * 获取对应的provider
     * 
     * @param group
     * @param service
     * @return
     */
    public List<BeaconPath> getLocalProviders(String group, String service);

    /**
     * 获取本地的service信息
     * 对应的consumer本地只存储自身的那一个
     * 
     * @param service
     * @return
     */
    public BeaconPath getLocalConsumer(String service);

    /**
     * 获取代理对象,用于server端调用
     * 
     * @param service
     * @return
     */
    public Object getProxyBean(String service);

}
