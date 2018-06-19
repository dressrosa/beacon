/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.core.register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    /**
     * service->proxyBean
     */
    private static final Map<String, Object> BEAN_MAP = new HashMap<>(32);

    protected void addProxyBean(BeaconPath beaconPath) {
        if(beaconPath.getProxy() != null) {
            BEAN_MAP.put(beaconPath.getService(), beaconPath.getProxy());
        }
    }

    @Override
    public Object getProxyBean(String service) {
        return BEAN_MAP.get(service);
    }

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
    public List<BeaconPath> getLocalProviders(String service) {
        Set<BeaconPath> providers = SERVICE_MAP.get(service);
        List<BeaconPath> list = new ArrayList<>();
        for (BeaconPath p : providers) {
            if (p.getSide() == From.SERVER) {
                list.add(p);
            }
        }
        return list;
    }

    @Override
    public BeaconPath getLocalConsumer(String service) {
        Set<BeaconPath> providers = SERVICE_MAP.get(service);
        if(providers != null) {
            for (BeaconPath p : providers) {
                if (p.getSide() == From.CLIENT) {
                    return p;
                }
            }
        }
        return null;
    }

    protected void initProviders(String service) {
        if (!SERVICE_MAP.containsKey(service)) {
            SERVICE_MAP.put(service, new HashSet<>());
        }
        doInitProviders(service);
    }

    protected void storeLocalService(String service, BeaconPath path) {
        if (!SERVICE_MAP.containsKey(service)) {
            SERVICE_MAP.put(service, new HashSet<>());
        }
        doStoreLocalService(service, path);
    }

    /**
     * service存储在本地
     * 
     * @param service
     * @param path
     */
    public abstract void doStoreLocalService(String service, BeaconPath path);

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
