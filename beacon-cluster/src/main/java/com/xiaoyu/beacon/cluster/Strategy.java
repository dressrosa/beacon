package com.xiaoyu.beacon.cluster;

import com.xiaoyu.beacon.common.bean.BeaconPath;
import com.xiaoyu.beacon.rpc.config.bean.Invocation;

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
