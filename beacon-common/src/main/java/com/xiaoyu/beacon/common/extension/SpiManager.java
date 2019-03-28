/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.common.extension;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.xiaoyu.beacon.common.utils.StringUtil;

/**
 * 存储所有扩展的类
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class SpiManager {

    /**
     * targetClass->extendHolder
     */
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
        ExtenderHolder<T> holder = (ExtenderHolder<T>) SPI_MAP.get(cls);
        if (holder == null) {
            SPI_MAP.put(cls, new ExtenderHolder<>());
        }
        holder = (ExtenderHolder<T>) SPI_MAP.get(cls);
        if (!holder.isEmpty()) {
            if (StringUtil.isEmpty(holder.getDefault_key())) {
                return holder.randomOne();
            } else {
                return holder.target(holder.getDefault_key());
            }
        }
        T t = null;
        // TODO 首次并发加载可能会加载多次
        BeaconServiceLoader<T> loader = BeaconServiceLoader.load(cls);
        Iterator<T> iter = loader.iterator();
        // 存储所有的,取最后一个
        while (iter.hasNext()) {
            t = iter.next();
            holder.put(loader.getProtocolName(), t);
        }
        if (t == null) {
            throw new Exception("Cannot find spi of " + cls.getName());
        }
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
            SPI_MAP.put(cls, new ExtenderHolder<>());
        }
        holder = (ExtenderHolder<T>) SPI_MAP.get(cls);
        if (!holder.isEmpty()) {
            return holder;
        }
        // TODO 首次并发加载可能会加载多次
        BeaconServiceLoader<T> loader = BeaconServiceLoader.load(cls);
        Iterator<T> iter = loader.iterator();
        while (iter.hasNext()) {
            t = iter.next();
            holder.put(loader.getProtocolName(), t);
        }
        return holder;
    }

}
