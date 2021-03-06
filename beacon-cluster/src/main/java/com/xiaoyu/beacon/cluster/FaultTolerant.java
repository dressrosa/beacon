/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.cluster;

import java.util.List;

import com.xiaoyu.beacon.common.bean.BeaconPath;
import com.xiaoyu.beacon.rpc.config.bean.Invocation;

/**
 * @author hongyu
 * @date 2018-05
 * @description 容错机制
 */
public interface FaultTolerant {

    /**
     * 根据规则进行失败容错
     * 
     * @param providers
     * @return
     * @throws Throwable
     */
    public Object invoke(Invocation invocation, List<BeaconPath> providers) throws Throwable;

}
