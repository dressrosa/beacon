/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.beacon.filter.generic;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.xiaoyu.beacon.common.bean.BeaconPath;
import com.xiaoyu.beacon.common.constant.BeaconConstants;
import com.xiaoyu.beacon.common.constant.From;
import com.xiaoyu.beacon.common.extension.SpiManager;
import com.xiaoyu.beacon.common.message.RpcRequest;
import com.xiaoyu.beacon.common.utils.NetUtil;
import com.xiaoyu.beacon.filter.api.Filter;
import com.xiaoyu.beacon.registry.Registry;
import com.xiaoyu.beacon.rpc.config.bean.Invocation;

/**
 * 泛型调用过滤器
 * 
 * @author hongyu
 * @date 2018-07
 * @description
 */
public class GenericFilter implements Filter {

    /**
     * beanconPath.toPath->consumer beaconPath
     */
    private static final ConcurrentMap<String, BeaconPath> Consumer_Map = new ConcurrentHashMap<>(16);

    private static Registry registry = null;

    static {
        try {
            registry = SpiManager.defaultSpiExtender(Registry.class);
        } catch (Exception e) {
            // do nothing
        }
    }

    @Override
    public void invoke(Invocation invocation) {
        BeaconPath con = invocation.getConsumer();
        if (!con.isGeneric()) {
            return;
        }
        RpcRequest req = invocation.getRequest();
        if (!BeaconConstants.$_$INVOKE.equals(req.getMethodName())) {
            return;
        }
        con.setService(req.getInterfaceName())
                .setCheck(false)
                .setHost(NetUtil.localIP())
                .setRetry(0)
                .setSide(From.CLIENT);
        String key = con.toPath();
        if (Consumer_Map.containsKey(key)) {
            invocation.setConsumer(Consumer_Map.get(key));
        } else {
            invocation.setConsumer(con);
            // for safe concurrent
            if (Consumer_Map.put(key, con) == null) {
                // (可忽略).高并发下,可能导致这里同个con还没注册完,其他线程就已经开始执行了
                registry.registerService(con);
            }
        }
        Object[] params = req.getParams();
        req.setMethodName((String) params[0])
                .setParams((Object[]) params[2])
                .setReturnType(params[1]);
    }
}
