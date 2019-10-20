/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.rpc.config.bean;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.beacon.common.bean.BeaconMethod;
import com.xiaoyu.beacon.common.bean.BeaconPath;
import com.xiaoyu.beacon.common.exception.BeaconException;
import com.xiaoyu.beacon.common.exception.BizException;
import com.xiaoyu.beacon.common.extension.SpiManager;
import com.xiaoyu.beacon.common.message.RpcRequest;
import com.xiaoyu.beacon.common.message.RpcResponse;
import com.xiaoyu.beacon.common.utils.StringUtil;
import com.xiaoyu.beacon.rpc.api.Context;

/**
 * 调用封装
 * 
 * @author hongyu
 * @date 2018-05
 * @description
 */
public class Invocation {

    private static final Logger LOG = LoggerFactory.getLogger(Invocation.class);

    private BeaconPath consumer;

    private RpcRequest request;

    public Invocation(BeaconPath consumer, RpcRequest request) {
        this.consumer = consumer;
        this.request = request;
        request.setTimeout(Long.valueOf(consumer.getTimeout()));
        // 检查是否有方法级别的信息配置
        List<BeaconMethod> beaconMethods = this.consumer.getBeaconMethods();
        if (beaconMethods != null) {
            for (BeaconMethod m : beaconMethods) {
                if (request.getMethodName().equals(m.getMethodName())) {
                    request.setTimeout(m.getTimeout());
                    break;
                }
            }
        }
    }

    public BeaconPath getConsumer() {
        return consumer;
    }

    public RpcRequest getRequest() {
        return request;
    }

    public Invocation setConsumer(BeaconPath consumer) {
        this.consumer = consumer;
        return this;
    }

    public Object invoke(BeaconPath provider) throws Throwable {
        request.setInterfaceImpl(provider.getRef());
        String[] mes = provider.getMethods().split(",");
        boolean access = false;
        String methodName = request.getMethodName();
        for (int i = 0; i < mes.length; i++) {
            if (methodName.equals(mes[i])) {
                access = true;
                break;
            }
        }
        if (!access) {
            throw new BeaconException("Have no access to invoke the method " + request.getMethodName() + " in "
                    + request.getInterfaceName());
        }

        // 发送消息
        Object ret = SpiManager.defaultSpiExtender(Context.class)
                .client(provider.getHost(), Integer.valueOf(provider.getPort()))
                .send(request);
        RpcResponse result = (RpcResponse) ret;
        Throwable ex = result.getException();
        if (ex != null) {
            LOG.error("Beacon exception->", ex);
            if (ex instanceof BizException) {
                throw ex.getCause();
            }
            throw ex;
        }
        // 我们忽略exception,所以直接使用errorMessage当作异常
        else if (StringUtil.isNotEmpty(result.getErrorMessage())) {
            throw new RuntimeException(result.getErrorMessage());
        }
        return result.getResult();
    }

}
