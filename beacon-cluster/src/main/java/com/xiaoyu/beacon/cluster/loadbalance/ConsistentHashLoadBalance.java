package com.xiaoyu.beacon.cluster.loadbalance;

import java.util.HashMap;
import java.util.HashSet;
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
     * service->HashSet (true providers,这里用来判断是否已经加入SortedMap)
     */
    private static final Map<String, HashSet<String>> Service_Providers = new HashMap<>(32);

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

        this.doCheckRefresh(pros);

        // 以service为key,这样保证同一个service请求同一个server
        String service = pros.get(0).getService();
        // host->BeaconPath
        Map<String, BeaconPath> proMap = this.spreadTrueProviders(pros);

        SortedMap<Integer, String> cacheMap = Service_Hash_Cache.get(service);
        // 找比她大的所有节点
        SortedMap<Integer, String> nodes = cacheMap.tailMap(hash(service));
        int key;
        String host = null;
        // 说明已是环的终点
        if (nodes.isEmpty()) {
            // 返回开头,直接取第一个节点
            key = Service_Hash_Cache.get(service).firstKey();
            String value = cacheMap.get(key);
            // 取出虚拟节点中的host
            host = value.substring(value.indexOf(Separator) + 1);
        } else {
            // 取下一个节点
            key = hash(service);
            String value = cacheMap.get(nodes.firstKey());
            // 取出虚拟节点中的host
            host = value.substring(value.indexOf(Separator) + 1);
        }
        return (T) proMap.get(host);
    }

    /**
     * 检查是否server已经不一致,需清空sorted_Map和providers
     * 
     * @param pros
     */
    private void doCheckRefresh(List<BeaconPath> pros) {
        String service = pros.get(0).getService();
        // 这里在首次并发的时候也不影响
        HashSet<String> sets = Service_Providers.get(service);
        if (sets == null) {
            Service_Providers.put(service, (sets = new HashSet<>()));
        }
        SortedMap<Integer, String> cacheMap = Service_Hash_Cache.get(service);
        if (cacheMap == null) {
            Service_Hash_Cache.put(service, (cacheMap = new TreeMap<>()));
        }
        if (!sets.isEmpty()) {
            // 旧server下线
            if (sets.size() != pros.size()) {
                Read_Write_Lock.writeLock().lock();
                try {
                    cacheMap.clear();
                    sets.clear();
                } finally {
                    Read_Write_Lock.writeLock().unlock();
                }
            } else {
                String pst = null;
                // 新server上线
                for (BeaconPath p : pros) {
                    if (!sets.contains(pst = p.getHost() + ":" + p.getPort())) {
                        // 这里写锁,一旦进入,其他都需要等待clear完成,这个可能影响其他service.
                        // 不过server下线的情况是会很少出现的
                        Read_Write_Lock.writeLock().lock();
                        try {
                            // 这里再判断一次,因为在读锁释放后,可能这里已经contain了
                            if (!sets.contains(pst)) {
                                cacheMap.clear();
                                sets.clear();
                            }
                        } finally {
                            Read_Write_Lock.writeLock().unlock();
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * 根据真实节点生成虚拟节点
     * 
     * @param providers
     * @return
     */
    private Map<String, BeaconPath> spreadTrueProviders(List<BeaconPath> providers) {
        Map<String, BeaconPath> proMap = new HashMap<>(providers.size());
        String service = providers.get(0).getService();
        Read_Write_Lock.readLock().lock();
        // 这里读锁,每个线程都可以进入
        HashSet<String> sets = Service_Providers.get(service);
        try {
            String pst = null;
            for (BeaconPath p : providers) {
                proMap.put((pst = p.getHost() + ":" + p.getPort()), p);
                if (sets.add(pst)) {
                    spreadVirtualProvider(p);
                }
            }
        } finally {
            Read_Write_Lock.readLock().unlock();
        }
        return proMap;
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
        SortedMap<Integer, String> cacheMap = Service_Hash_Cache.get(p.getService());
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
