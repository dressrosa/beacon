package com.xiaoyu.core.common.extension;

import java.util.Iterator;
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

    private static final ConcurrentMap<Class<?>, ExtenderHolder<?>> SPI_MAP = new ConcurrentHashMap<>(16);

    /**
     * 返回默认一个
     * 
     * @param cls
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T defaultSpiExtender(Class<T> cls) throws Exception {
        T t = null;
        ExtenderHolder<T> holder = (ExtenderHolder<T>) SPI_MAP.get(cls);
        if (holder == null) {
            holder = new ExtenderHolder<>();
        }
        if (!holder.isEmpty()) {
            return holder.randomOne();
        }
        BeaconServiceLoader<T> loader = BeaconServiceLoader.load(cls);
        Iterator<T> iter = loader.iterator();
        // 存储所有的,取最后一个
        while (iter.hasNext()) {
            t = iter.next();
            holder.put(loader.getProtocolName(), t);
        }
        if (t == null) {
            throw new Exception("cannot find spi of " + cls.getName());
        }
        SPI_MAP.putIfAbsent(cls, holder);
        return t;
    }

    /**
     * 返回cls的holder,可供指定的选择
     * 
     * @param cls
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> ExtenderHolder<T> holder(Class<T> cls) {
        T t = null;
        ExtenderHolder<T> holder = (ExtenderHolder<T>) SPI_MAP.get(cls);
        if (holder == null) {
            holder = new ExtenderHolder<>();
        }
        if (!holder.isEmpty()) {
            return holder;
        }
        BeaconServiceLoader<T> loader = BeaconServiceLoader.load(cls);
        Iterator<T> iter = loader.iterator();
        while (iter.hasNext()) {
            t = iter.next();
            holder.put(loader.getProtocolName(), t);
        }
        SPI_MAP.putIfAbsent(cls, holder);
        return holder;
    }

}
