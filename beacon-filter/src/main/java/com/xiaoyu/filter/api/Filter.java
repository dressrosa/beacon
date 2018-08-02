/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.filter.api;

import com.xiaoyu.core.rpc.config.bean.Invocation;

/**
 * 过滤器
 * 
 * @author hongyu
 * @date 2018-07
 * @description
 */
public interface Filter {

    /**
     * 进行过滤处理
     * 
     * @param invocation
     */
    public void invoke(Invocation invocation);
}
