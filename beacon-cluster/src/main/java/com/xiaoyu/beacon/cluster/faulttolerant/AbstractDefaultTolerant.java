/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.cluster.faulttolerant;

import java.util.List;

import com.xiaoyu.beacon.cluster.FaultTolerant;
import com.xiaoyu.beacon.cluster.LoadBalance;
import com.xiaoyu.beacon.cluster.Strategy;
import com.xiaoyu.beacon.common.bean.BeaconPath;
import com.xiaoyu.beacon.common.extension.SpiManager;
import com.xiaoyu.beacon.common.utils.StringUtil;
import com.xiaoyu.beacon.rpc.config.bean.Invocation;

/**
 * @author hongyu
 * @date 2018-12
 * @description 提供默认的调用
 */
public class AbstractDefaultTolerant implements FaultTolerant {

    @Override
    public Object invoke(Invocation invocation, List<BeaconPath> providers) throws Throwable {
        // 负载均衡
        LoadBalance loadBalance = SpiManager.defaultSpiExtender(LoadBalance.class);
        BeaconPath provider = loadBalance.select(providers);
        if (StringUtil.isBlank(invocation.getConsumer().getDowngrade())) {
            return invocation.invoke(provider);
        }
        // 熔断降级
        Strategy strategy = SpiManager.defaultSpiExtender(Strategy.class);
        return strategy.fuse(invocation, provider);
    }

}
