package com.xiaoyu.core.cluster.loadbalance;

import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.xiaoyu.core.cluster.LoadBalance;
import com.xiaoyu.core.common.bean.BeaconPath;

/**
 * 一致性hash
 * 
 * @author hongyu
 * @date 2018-06
 * @description 尽量保证同一个请求service尽量每次都映射到同一个server,这样可以保证server端的缓存有效性
 */
public class ConsistentHashLoadBalance implements LoadBalance {

    /**
     * hash->virtual machine
     */
    private static final SortedMap<Integer, String> Circle_Sorted_Map = new TreeMap<>();

    /**
     * true machine
     */
    private static final HashSet<String> Machines = new HashSet<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T select(List<T> providers) {
        if (providers.size() == 1) {
            return providers.get(0);
        }
        List<BeaconPath> pros = (List<BeaconPath>) providers;
        String service = pros.get(0).getService();
        initMachines(pros);

        // 找比他大的所以节点
        SortedMap<Integer, String> virtuals = Circle_Sorted_Map.tailMap(hash(service));
        int key;
        String host;
        // 环的终点
        if (virtuals.isEmpty()) {
            key = Circle_Sorted_Map.firstKey();
            host = Circle_Sorted_Map.get(key).split(",")[0];
        } else {
            key = hash(service);
            int firstKey = virtuals.firstKey();
            host = Circle_Sorted_Map.get(firstKey).split(",")[0];
        }
        for (int i = 0; i < pros.size(); i++) {
            if (host.equals(pros.get(i).getService())) {
                return (T) pros.get(i);
            }
        }
        return providers.get(0);
    }

    /**
     * 加入真实节点
     * 
     * @param providers
     */
    private void initMachines(List<BeaconPath> providers) {
        for (BeaconPath p : providers) {
            if (Machines.add(p.getHost())) {
                spreadVirtualMachine(p.getHost());
            }
        }
    }

    /**
     * 存储虚拟节点
     * 
     * @param host
     */
    private void spreadVirtualMachine(String host) {
        String virtual;
        int num = Machines.size() << 5;
        for (int i = 0; i < num; i++) {
            virtual = host + "," + i;
            Circle_Sorted_Map.put(hash(virtual), virtual);
        }
    }

    /**
     * FNV1_32_HASH
     * 
     * @param str
     * @return
     */
    private static int hash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash ^ str.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        if (hash < 0) {
            // 取相反数
            hash = ~hash + 1;
        }
        return hash;
    }

}
