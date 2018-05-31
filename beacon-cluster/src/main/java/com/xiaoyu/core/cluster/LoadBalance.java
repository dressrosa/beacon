/**
 * 唯有读书,不慵不扰
 * 
 */
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
    public <T> T select(final List<T> providers);

}
