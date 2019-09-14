/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.cluster.faulttolerant;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.beacon.cluster.LoadBalance;
import com.xiaoyu.beacon.cluster.Strategy;
import com.xiaoyu.beacon.common.bean.BeaconPath;
import com.xiaoyu.beacon.common.exception.BizException;
import com.xiaoyu.beacon.common.extension.SpiManager;
import com.xiaoyu.beacon.common.utils.StringUtil;
import com.xiaoyu.beacon.rpc.config.bean.Invocation;

/**
 * 失败转移
 * 
 * @author hongyu
 * @date 2018-06
 * @description 一个provider失败,转而调用其他的provider
 */
public class FailOverTolerant extends AbstractDefaultTolerant {

    private static final Logger LOG = LoggerFactory.getLogger(FailOverTolerant.class);

    @Override
    public Object invoke(Invocation invocation, List<BeaconPath> providers) throws Throwable {
        BeaconPath provider = null;
        try {
            // 首次调用需要负载均衡
            LoadBalance loadBalance = SpiManager.defaultSpiExtender(LoadBalance.class);
            provider = loadBalance.select(providers);
            return doInvoke(invocation, provider);
        } catch (Throwable e) {
            // 业务类异常,直接正常抛出
            if (e instanceof BizException) {
                throw e;
            } else {
                if (providers.size() > 1) {
                    providers.remove(provider);
                    return doOver(invocation, providers);
                } else {
                    throw e;
                }
            }
        }
    }

    private Object doOver(Invocation invocation, List<BeaconPath> providers) throws Throwable {
        // 转移采用依次调用
        int size = providers.size();
        if (size == 1) {
            return doInvoke(invocation, providers.get(0));
        }
        // 轮询到倒数第二个
        for (int i = 0; i < size - 1; i++) {
            try {
                return doInvoke(invocation, providers.get(i));
            } catch (Throwable e) {
                // 业务类异常,直接正常抛出
                if (e instanceof BizException) {
                    throw e;
                } else {
                    LOG.error("failover error->" + e);
                    continue;
                }
            }
        }
        // 轮询到倒数第二个,是为了不return null,因为无法判断返回值是否应该是null
        return doInvoke(invocation, providers.get(size - 1));
    }

    private Object doInvoke(Invocation invocation, BeaconPath provider) throws Throwable {
        if (StringUtil.isBlank(invocation.getConsumer().getDowngrade())) {
            return invocation.invoke(provider);
        }
        // 熔断降级
        Strategy strategy = SpiManager.defaultSpiExtender(Strategy.class);
        return strategy.fuse(invocation, provider);
    }
}
