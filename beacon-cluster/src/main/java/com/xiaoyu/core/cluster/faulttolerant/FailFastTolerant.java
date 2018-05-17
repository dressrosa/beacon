package com.xiaoyu.core.cluster.faulttolerant;

import java.util.List;

import com.xiaoyu.core.cluster.FaultTolerant;
import com.xiaoyu.core.cluster.LoadBalance;
import com.xiaoyu.core.common.bean.BeaconPath;
import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.common.message.RpcRequest;
import com.xiaoyu.core.common.message.RpcResponse;
import com.xiaoyu.core.rpc.api.Context;

/**
 * @author hongyu
 * @date 2018-05
 * @description 快速失败
 */
public class FailFastTolerant implements FaultTolerant {

    @Override
    public Object invoke(RpcRequest request, List<?> providers) throws Throwable {
        // 负载均衡
        LoadBalance loadBalance = SpiManager.defaultSpiExtender(LoadBalance.class);
        BeaconPath provider = (BeaconPath) loadBalance.select(providers);

        request.setInterfaceImpl(provider.getRef());
        // 发送消息
        Object ret = SpiManager.defaultSpiExtender(Context.class)
                .client(provider.getHost(), Integer.valueOf(provider.getPort())).send(request);
        RpcResponse result = (RpcResponse) ret;
        if (result.getException() != null) {
            throw result.getException();
        }
        return result.getResult();
    }

}
