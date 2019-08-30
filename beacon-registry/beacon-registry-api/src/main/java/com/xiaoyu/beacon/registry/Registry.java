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
    void address(String addr);

    /**
     * 关闭注册中心
     */
    void close();

    /**
     * 是否连接
     * 
     * @param addr
     * @return
     */
    boolean isInit();

    /**
     * 发现服务
     * 
     * @param service
     * @return
     */
    boolean discoverService(String service);

    /**
     * 注册服务
     * 
     * @param beaconPath
     */
    void registerService(BeaconPath beaconPath);

    /**
     * 取消注册服务
     * 
     * @param beaconPath
     */
    void unregisterService(BeaconPath beaconPath);

    /**
     * 获取对应的provider
     * 
     * @param group
     * @param service
     * @return
     */
    List<BeaconPath> getLocalProviders(String group, String service);

    /**
     * 获取本地的service信息
     * 对应的consumer本地只存储自身的那一个
     * 
     * @param service
     * @return
     */
    BeaconPath getLocalConsumer(String service);

    /**
     * 获取代理对象,用于server端调用
     * 
     * @param service
     * @return
     */
    Object getProxyBean(String service);

}
