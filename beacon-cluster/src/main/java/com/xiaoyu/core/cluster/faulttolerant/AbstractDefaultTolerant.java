/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.core.cluster.faulttolerant;

import java.util.List;

import com.xiaoyu.core.cluster.FaultTolerant;
import com.xiaoyu.core.cluster.LoadBalance;
import com.xiaoyu.core.cluster.Strategy;
import com.xiaoyu.core.common.bean.BeaconPath;
import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.common.utils.StringUtil;
import com.xiaoyu.core.rpc.config.bean.Invocation;

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
        BeaconPath provider = null;
        if (StringUtil.isBlank(invocation.getConsumer().getDowngrade())) {
            provider = loadBalance.select(providers);
            return invocation.invoke(provider);
        }
        // 熔断降级
        Strategy strategy = SpiManager.defaultSpiExtender(Strategy.class);
        provider = loadBalance.select(providers);
        return strategy.fuse(invocation, provider);
    }

}
