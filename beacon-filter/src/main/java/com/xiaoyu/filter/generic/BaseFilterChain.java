/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.filter.generic;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.xiaoyu.core.common.extension.ExtenderHolder;
import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.rpc.config.bean.Invocation;
import com.xiaoyu.filter.api.Filter;
import com.xiaoyu.filter.api.FilterChain;

/**
 * 调用链
 * 
 * @author hongyu
 * @date 2018-07
 * @description 初始化调用链,通过调用链对所有的filter进行调用,此类的spi是必须存在的
 */
public class BaseFilterChain implements FilterChain {

    private static final LinkedHashSet<Filter> filterList = new LinkedHashSet<>();

    // static只会在类初始化的时候执行一次,所以就不会有并发问题
    static {
        ExtenderHolder<Filter> holder = SpiManager.holder(Filter.class);
        Collection<Filter> col = holder.values();
        if (!col.isEmpty()) {
            for (Filter f : col) {
                // 去除BaseFilterChain
                if (f instanceof FilterChain) {
                    continue;
                }
                filterList.add(f);
            }
        }
    }

    @Override
    public void invoke(Invocation invocation) {
        Iterator<Filter> iter = filterList.iterator();
        Filter filter = null;
        while (iter.hasNext()) {
            filter = iter.next();
            filter.invoke(invocation);
        }
    }
}
