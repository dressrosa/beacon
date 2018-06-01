/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.core.cluster.faulttolerant;

import java.util.List;

import com.xiaoyu.core.cluster.FaultTolerant;
import com.xiaoyu.core.cluster.LoadBalance;
import com.xiaoyu.core.common.bean.BeaconPath;
import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.rpc.config.bean.Invocation;

/**
 * @author hongyu
 * @date 2018-05
 * @description 快速失败
 */
public class FailFastTolerant implements FaultTolerant {

    @Override
    public Object invoke(Invocation invocation, List<?> providers) throws Throwable {
        // 负载均衡
        LoadBalance loadBalance = SpiManager.defaultSpiExtender(LoadBalance.class);
        BeaconPath provider = (BeaconPath) loadBalance.select(providers);
        return invocation.invoke(provider);
    }

}
