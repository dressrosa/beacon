/**
 * 唯有读书,不慵不扰
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

    /**
     * provider service->BeaconPath(serviceDetail)
     */
    protected static final ConcurrentMap<String, Set<BeaconPath>> Provider_Service_Map = new ConcurrentHashMap<>(32);
    /**
     * consumer service->BeaconPath(serviceDetail)
     */
    protected static final ConcurrentMap<String, Set<BeaconPath>> Consumer_Service_Map = new ConcurrentHashMap<>(32);

    @Override
    public Object getProxyBean(String service) {
        // provider端的service对应的provider只有一个
        return Provider_Service_Map.get(service).iterator().next().getProxy();
    }

    @Override
    public boolean discoverService(String service) {
        Set<BeaconPath> sets = Provider_Service_Map.get(service);
        if (sets != null) {
            return true;
        }
        return this.doDiscoverService(service);
    }

    @Override
    public List<BeaconPath> getLocalProviders(String group, String service) {
        Set<BeaconPath> providers = Provider_Service_Map.get(service);
        List<BeaconPath> list = new ArrayList<>();
        for (BeaconPath p : providers) {
            if (p.getGroup().equals(group)) {
                list.add(p);
            }
        }
        return list;
    }

    @Override
    public BeaconPath getLocalConsumer(String service) {
        // 在consumer端set里面只有一个
        return Consumer_Service_Map.get(service).iterator().next();
    }

    protected void initProviders(String service) {
        if (!Provider_Service_Map.containsKey(service)) {
            Provider_Service_Map.put(service, new HashSet<>());
        }
        doInitProviders(service);
    }

    protected void storeLocalService(String service, BeaconPath path) {
        if (path.getSide() == From.CLIENT) {
            if (!Consumer_Service_Map.containsKey(service)) {
                Consumer_Service_Map.put(service, new HashSet<>());
            }
            Consumer_Service_Map.get(service).add(path);
        } else {
            if (!Provider_Service_Map.containsKey(service)) {
                Provider_Service_Map.put(service, new HashSet<>());
            }
            Provider_Service_Map.get(service).add(path);
        }
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
