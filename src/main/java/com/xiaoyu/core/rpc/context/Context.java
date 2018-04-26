package com.xiaoyu.core.rpc.context;

import com.xiaoyu.core.register.Registry;
import com.xiaoyu.transport.api.Client;

/**
 * @author hongyu
 * @date 2018-04
 * @description
 */
public interface Context {

    public Client client() throws Exception;

    public void registry(Registry registry);
}
