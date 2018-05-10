package com.xiaoyu.core.register;

import java.util.List;

import com.xiaoyu.core.rpc.config.bean.BeaconPath;

/**
 * @author hongyu
 * @date 2018-02
 * @description
 */
public interface Registry {

    /**
     * 地址
     * 
     * @param addr
     */
    public void address(String addr);

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
     */
    public void registerService(BeaconPath beaconPath);

    /**
     * 取消注册服务
     */
    public void unregisterService(BeaconPath beaconPath);

    public List<BeaconPath> getProviders(String service);
}
