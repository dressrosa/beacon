package com.xiaoyu.core.register;

import com.xiaoyu.core.common.constant.From;

/**
 * @author hongyu
 * @date 2018-02
 * @description
 */
public interface Registry {

    /**
     * 发现服务
     * 
     * @param service
     * @return
     */
    public boolean discoverService(String service);

    /**
     * 取消注册服务
     */
    public void unregisterService();

    /**
     * @param service
     * @param side
     */
    public void registerService(String service, From side);
}
