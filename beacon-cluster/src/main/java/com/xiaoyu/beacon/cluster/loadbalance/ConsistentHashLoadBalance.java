package com.xiaoyu.beacon.cluster.loadbalance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.xiaoyu.beacon.cluster.LoadBalance;
import com.xiaoyu.beacon.common.bean.BeaconPath;

/**
 * 一致性hash
 * 
 * @author hongyu
 * @date 2018-06
 * @description 尽量保证同一个请求service每次都映射到同一个server,这样可以保证server端的缓存有效性
 */
public class ConsistentHashLoadBalance implements LoadBalance {

    /**
     * service->SortedMap(hash->virtual providers)
     */
    private static final Map<String, SortedMap<Integer, String>> Service_Hash_Cache = new HashMap<>(32);
    /**
     * service->HashMap (true providers,这里用来判断是否已经加入SortedMap)
     */
    private static final Map<String, Map<String, BeaconPath>> Service_Providers = new HashMap<>(32);

    /**
     * 这里读写锁,用来对server端下线后,服务不一致时候,对Sorted_Map进行clear的操作
     * clear加写锁,add加读锁
     */
    private static final ReentrantReadWriteLock Read_Write_Lock = new ReentrantReadWriteLock();

    /**
     * 格式-> 12,192.168.0.0:8080
     */
    private static final String Separator = ",";

    @SuppressWarnings("unchecked")
    @Override
    public <T> T select(List<T> providers) {
        int size = providers.size();
        if (size == 1) {
            return providers.get(0);
        }
        List<BeaconPath> pros = (List<BeaconPath>) providers;

        boolean refreshed = this.doCheckRefresh(pros);
        if (refreshed) {
            this.spreadTrueProviders(pros);
        }
        // 以service为key,这样保证同一个service请求同一个server
        String service = pros.get(0).getService();
        // host->BeaconPath
        final Map<String, BeaconPath> proMap = Service_Providers.get(service);
        final SortedMap<Integer, String> cacheMap = Service_Hash_Cache.get(service);

        // 找比她大的所有节点
        SortedMap<Integer, String> nodes = cacheMap.tailMap(hash(service));
        String host = null;
        // 说明已是环的终点
        String value = null;
        if (nodes.isEmpty()) {
            // 返回开头,直接取第一个节点
            value = cacheMap.get(cacheMap.firstKey());
        } else {
            value = cacheMap.get(nodes.firstKey());
        }
        // 取出虚拟节点中的host
        host = value.substring(value.indexOf(Separator) + 1);
        return (T) proMap.get(host);
    }

    /**
     * 检查是否server已经不一致,需清空sorted_Map和providers
     * 
     * @param pros
     */
    private boolean doCheckRefresh(List<BeaconPath> pros) {
        String service = pros.get(0).getService();
        // 这里在首次并发的时候也不影响
        Map<String, BeaconPath> trueMap = Service_Providers.get(service);
        if (trueMap == null) {
            Service_Providers.put(service, (trueMap = new HashMap<>(pros.size())));
        }
        SortedMap<Integer, String> cacheMap = Service_Hash_Cache.get(service);
        if (cacheMap == null) {
            Service_Hash_Cache.put(service, (cacheMap = new TreeMap<>()));
        }
        boolean refreshed = false;
        if (trueMap.isEmpty()) {
            return true;
        }
        final ReentrantReadWriteLock lock = Read_Write_Lock;
        // 旧server下线
        if (trueMap.size() != pros.size()) {
            lock.writeLock().lock();
            try {
                cacheMap.clear();
                trueMap.clear();
                refreshed = true;
            } finally {
                lock.writeLock().unlock();
            }
        } else {
            String pst = null;
            for (BeaconPath p : pros) {
                // 新server上线
                if (!trueMap.containsKey(pst = p.getHost() + ":" + p.getPort())) {
                    // 这里写锁,一旦进入,其他都需要等待clear完成,这个可能影响其他service.
                    // 不过server下线的情况是会很少出现的
                    lock.writeLock().lock();
                    try {
                        // 这里再判断一次,因为在读锁释放后,可能这里已经contain了
                        if (!trueMap.containsKey(pst)) {
                            cacheMap.clear();
                            trueMap.clear();
                            refreshed = true;
                        }
                    } finally {
                        lock.writeLock().unlock();
                    }
                    break;
                }
            }
        }
        return refreshed;
    }

    /**
     * 根据真实节点生成虚拟节点
     * 
     * @param providers
     * @return
     */
    private void spreadTrueProviders(List<BeaconPath> providers) {
        String service = providers.get(0).getService();
        final ReentrantReadWriteLock lock = Read_Write_Lock;
        lock.readLock().lock();
        // 这里读锁,每个线程都可以进入
        final Map<String, BeaconPath> trueMap = Service_Providers.get(service);
        try {
            String pst = null;
            for (BeaconPath p : providers) {
                trueMap.put(pst = p.getHost() + ":" + p.getPort(), p);
                if (trueMap.containsKey(pst)) {
                    spreadVirtualProvider(p);
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 初始化虚拟节点(格式-> 12,127.0.0.1:8080)
     * 
     * @param host
     */
    private void spreadVirtualProvider(BeaconPath p) {
        String virtual = null;
        String appendStr = Separator.concat(p.getHost()).concat(":").concat(p.getPort());
        // 1:32
        int size = 32;
        final SortedMap<Integer, String> cacheMap = Service_Hash_Cache.get(p.getService());
        for (int i = 0; i < size; i++) {
            virtual = String.valueOf(i).concat(appendStr);
            cacheMap.put(hash(virtual), virtual);
        }
    }

    /**
     * FNV1_32_HASH
     * 保证hash的散列,来达到均匀铺开
     * 
     * @param str
     * @return
     */
    private static int hash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        int len = str.length();
        char[] chs = str.toCharArray();
        for (int i = 0; i < len; i++) {
            hash = (hash ^ chs[i]) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // hash负数取正数(相反数)
        if (hash < 0) {
            hash = ~hash + 1;
        }
        return hash;
    }

}
