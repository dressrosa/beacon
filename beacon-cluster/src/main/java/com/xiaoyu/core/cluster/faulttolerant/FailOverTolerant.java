/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.core.cluster.faulttolerant;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.core.cluster.FaultTolerant;
import com.xiaoyu.core.cluster.LoadBalance;
import com.xiaoyu.core.common.bean.BeaconPath;
import com.xiaoyu.core.common.exception.BizException;
import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.rpc.config.bean.Invocation;

/**
 * 失败重试
 * 
 * @author hongyu
 * @date 2018-06
 * @description 对非业务类失败,进行一定的重试
 */
public class FailOverTolerant implements FaultTolerant {

    private static final Logger LOG = LoggerFactory.getLogger(FailOverTolerant.class);

    @Override
    public Object invoke(Invocation invocation, List<?> providers) throws Throwable {
        // 负载均衡
        LoadBalance loadBalance = SpiManager.defaultSpiExtender(LoadBalance.class);
        BeaconPath provider = (BeaconPath) loadBalance.select(providers);
        Object result = null;
        try {
            result = invocation.invoke(provider);
        } catch (Throwable e) {
            // 业务类异常,直接正常抛出
            if (e instanceof BizException) {
                throw e;
            } else {
                // retry
                if (invocation.getConsumer().getRetry() > 0) {
                    return doRetry(invocation, providers);
                }
            }
            throw e;
        }
        return result;
    }

    private Object doRetry(Invocation invocation, List<?> providers) throws Throwable {
        int num = 0;
        LoadBalance loadBalance = SpiManager.defaultSpiExtender(LoadBalance.class);
        // 这里少retry一次,因为如果发生异常,最后一次的catch并没有抛出异常而是while退出了
        // 当然可以在continue之前做个if判断,不过...就是为了省个if,O(∩_∩)O~
        int retry = invocation.getConsumer().getRetry();
        BeaconPath provider = null;
        while (num++ < retry - 1) {
            provider = (BeaconPath) loadBalance.select(providers);
            Object result = null;
            try {
                LOG.info("Invoke failed, retry {} times", num);
                result = invocation.invoke(provider);
            } catch (Throwable e) {
                if (e instanceof BizException) {
                    throw e;
                } else {
                    TimeUnit.MILLISECONDS.sleep(500);
                    continue;
                }
            }
            return result;
        }
        // 这里不能直接返回null或result,需要再进行一次正常的调用
        provider = (BeaconPath) loadBalance.select(providers);
        return invocation.invoke(provider);
    }

}
