package com.xiaoyu.core.cluster;

import java.util.List;

/**
 * @author hongyu
 * @date 2018-05
 * @description 负载均衡
 */
public interface LoadBalance {

    /**
     * 根据规则选一个provider
     * 
     * @param providers
     * @return
     */
    public <T> T select(List<T> providers);

}
