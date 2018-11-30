package com.xiaoyu.core.cluster;

import com.xiaoyu.core.common.bean.BeaconPath;
import com.xiaoyu.core.rpc.config.bean.Invocation;

/**
 * 策略机制
 * 超时/故障重试/限流
 * 
 * @author hongyu
 * @date 2018-11
 * @description
 */
public interface Strategy {

    Object fuse(Invocation invocation, BeaconPath provider) throws Throwable;

}
