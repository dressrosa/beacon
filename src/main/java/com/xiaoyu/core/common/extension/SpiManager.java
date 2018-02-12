package com.xiaoyu.core.common.extension;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 存储所有扩展的类
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class SpiManager {

    private static final ConcurrentMap<Class<?>, ConcurrentMap<String, Object>> SPI_MAP = new ConcurrentHashMap<>(16);

    @SuppressWarnings("unchecked")
    public static <T> T defaultSpiExtender(Class<T> cls) throws Exception {
        T t = null;
        ConcurrentMap<String, Object> map = SPI_MAP.get(cls);
        if (map == null) {
            map = new ConcurrentHashMap<>(4);
        }
        if (!map.isEmpty()) {
            return (T) map.values().iterator().next();
        }
        ServiceLoader<T> loader = ServiceLoader.load(cls);
        Iterator<T> iter = loader.iterator();
        // t == null &&
        // 存储所有的,取最后一个
        while (iter.hasNext()) {
            t = iter.next();
            map.put(t.getClass().getName(), t);
        }
        if (t == null) {
            throw new Exception("not find spi of " + cls.getName());
        }
        SPI_MAP.putIfAbsent(cls, map);
        return t;
    }

}
