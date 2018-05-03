/**
 * 
 */
package com.xiaoyu.core.register;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author hongyu
 * @date 2018-05
 * @description
 */
public abstract class AbstractRegistry implements Registry {

    // service->serviceDetail
    protected static final ConcurrentMap<String, Set<String>> SERVICE_MAP = new ConcurrentHashMap<>(32);

    @Override
    public boolean discoverService(String service) {
        // 启动时client找到对应的providers信息,保存在本地缓存
        if (SERVICE_MAP.containsKey(service)) {
            return true;
        }
        return this.doDiscoverService(service);
    }

    public abstract boolean doDiscoverService(String service);

}
