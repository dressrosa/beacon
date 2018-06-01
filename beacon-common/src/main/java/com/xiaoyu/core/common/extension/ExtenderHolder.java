/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.core.common.extension;

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
            throw new Exception("cannot find the target->" + name);
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
}
