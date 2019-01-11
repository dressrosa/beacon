/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.core.common.extension;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 相当于concurrentMap
 * 
 * @author hongyu
 * @date 2018-05
 * @description
 */
public class ExtenderHolder<T> {

    private final ConcurrentMap<String, T> holder = new ConcurrentHashMap<>(16);

    /**
     * 当调用target方法时,设置为默认
     */
    private String default_key = null;

    public String getDefault_key() {
        return default_key;
    }

    public void setDefault_key(String default_key) {
        this.default_key = default_key;
    }

    public T target(String name) throws Exception {
        if (!holder.containsKey(name)) {
            // 在首次(首次划重点)并发加载的情况下,可能在SpiManger.holder()方法中,
            // BeaconLoader还未加载完,holder.isEmpty已经不为空,导致想取的数据还没有,这里
            // 睡眠10ms来缓冲.不过这种情况基本不会出现
            Thread.sleep(10);
            if (!holder.containsKey(name)) {
                throw new Exception("Cannot find the spi target->" + name);
            }
        }

        T t = (T) holder.get(name);
        setDefault_key(name);
        return t;
    }

    protected boolean isEmpty() {
        return holder.isEmpty();
    }

    protected void put(String key, T t) {
        holder.put(key, t);
    }

    protected int size() {
        return holder.size();
    }

    protected T randomOne() {
        // 返回一个能用的就行
        return holder.values().iterator().next();
    }

    public Collection<T> values() {
        return holder.values();
    }
}
