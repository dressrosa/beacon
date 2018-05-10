/**
 * 
 */
package com.xiaoyu.core.register;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.xiaoyu.core.common.constant.From;
import com.xiaoyu.core.rpc.config.bean.BeaconPath;

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

    @Override
    public List<BeaconPath> getProviders(String service) {
        Set<String> providers = SERVICE_MAP.get(service);
        List<BeaconPath> list = new ArrayList<>();
        BeaconPath p = null;
        for (String str : providers) {
            p = BeaconPath.toEntity(str);
            if (p.getSide() == From.SERVER) {
                list.add(p);
            }
        }
        return list;
    }

    protected void initProviders(String service) {
        if (!SERVICE_MAP.containsKey(service)) {
            SERVICE_MAP.put(service, new HashSet<>());
        }
        doInitProviders(service);
    }

    public abstract boolean doDiscoverService(String service);

    /**
     * 找到所有的providers
     * 
     * @param service
     */
    public abstract void doInitProviders(String service);

}
