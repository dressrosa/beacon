/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.proxy.api;

import java.util.List;
import java.util.Map;

import com.xiaoyu.core.cluster.FaultTolerant;
import com.xiaoyu.core.common.bean.BeaconPath;
import com.xiaoyu.core.common.bean.ProxyWrapper;
import com.xiaoyu.core.common.constant.BeaconConstants;
import com.xiaoyu.core.common.exception.BeaconException;
import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.common.message.RpcRequest;
import com.xiaoyu.core.register.Registry;
import com.xiaoyu.core.rpc.api.Context;
import com.xiaoyu.core.rpc.config.bean.Invocation;
import com.xiaoyu.filter.api.Filter;

/**
 * @author hongyu
 * @date 2018-04
 * @description
 */
public class InvocationHandlerAdapter extends AbstractInvocationHandler {

    public InvocationHandlerAdapter(Class<?> ref) {
        super(ref);
    }

    public InvocationHandlerAdapter(ProxyWrapper wrapper) {
        super(wrapper);
    }

    /**
     * 检测service是否存在;获取一个client;等待请求结果;
     * TODO 是否大部分工作都在这里完成需要再考虑
     * 
     * @param req
     * @return
     * @throws Throwable
     */
    @Override
    public Object doInvoke(RpcRequest request) throws Throwable {
        Registry reg = SpiManager.defaultSpiExtender(Context.class).getRegistry();
        // 判断service是否存在
        String service = request.getInterfaceName();
        boolean exist = reg.discoverService(service);
        // TODO 可能是注册中心数据丢失,并不是server掉线.这里抛异常可能会导致需要等待下次恢复检查才能继续进行.
        if (!exist) {
            throw new BeaconException(
                    "Cannot find the service->" + service + ";please check whether server start or not.");
        }
        // TODO 这里需要获取本地local的consumer,来获取调用信息传入
        BeaconPath consumer = doGetConsumer(service);
        Invocation invocation = new Invocation(consumer, request);
        // 过滤器
        Filter filter = SpiManager.holder(Filter.class).target(BeaconConstants.FILTER_CHAIN);
        filter.invoke(invocation);

        // 获取对应的全部provider
        List<BeaconPath> providers = reg.getLocalProviders(consumer.getGroup(), service);
        // 同group下无provider
        if (providers.isEmpty()) {
            throw new BeaconException(
                    "Cannot find the service->" + service + " in the group->" + consumer.getGroup()
                            + ";please check whether service belong to this group.");
        }
        // 容错调用
        FaultTolerant tolerant = SpiManager.holder(FaultTolerant.class).target(consumer.getTolerant());
        return tolerant.invoke(invocation, providers);
    }

    private BeaconPath doGetConsumer(String service) throws Exception {
        if (!wrapper.isGeneric()) {
            return SpiManager.defaultSpiExtender(Context.class).getRegistry().getLocalConsumer(service);
        }

        Map<String, Object> attach = wrapper.getAttach();
        BeaconPath con = new BeaconPath();
        con.setGeneric(true)
                .setTolerant((String) attach.get("tolerant"))
                .setTimeout((String) attach.get("timeout"))
                .setGroup((String) attach.get("group"));
        return con;
    }
}
