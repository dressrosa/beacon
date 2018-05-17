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

import com.xiaoyu.core.common.bean.BeaconPath;
import com.xiaoyu.core.common.constant.From;

/**
 * @author hongyu
 * @date 2018-05
 * @description
 */
public abstract class AbstractRegistry implements Registry {

    //
    /**
     * service->BeaconPath(serviceDetail)
     */
    protected static final ConcurrentMap<String, Set<BeaconPath>> SERVICE_MAP = new ConcurrentHashMap<>(32);

    @Override
    public boolean discoverService(String service) {
        Set<BeaconPath> sets = SERVICE_MAP.get(service);
        if (sets != null) {
            for (BeaconPath p : sets) {
                if (p.getSide() == From.SERVER) {
                    return true;
                }
            }
        }

        return this.doDiscoverService(service);
    }

    @Override
    public List<BeaconPath> getProviders(String service) {
        Set<BeaconPath> providers = SERVICE_MAP.get(service);
        List<BeaconPath> list = new ArrayList<>();
        for (BeaconPath p : providers) {
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

    /**
     * 是否存在注册service
     * 
     * @param service
     * @return
     */
    public abstract boolean doDiscoverService(String service);

    /**
     * 找到所有的providers
     * 
     * @param service
     */
    public abstract void doInitProviders(String service);

}
