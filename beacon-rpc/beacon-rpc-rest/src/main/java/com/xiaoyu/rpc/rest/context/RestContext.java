/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.rpc.rest.context;

import com.xiaoyu.core.rpc.context.AbstractBeaconContext;
import com.xiaoyu.transport.api.Client;
import com.xiaoyu.transport.api.Server;
import com.xiaoyu.transport.http.HttpClient;

/**
 * @author hongyu
 * @date 2018-08
 * @description 启用rest模式将以http方式请求
 */
public class RestContext extends AbstractBeaconContext {

    @Override
    public Client doInitClient(String host, int port) throws Exception {
        Client client = new HttpClient(host, port);
        return client;
    }

    @Override
    public Server doInitServer(Integer port) throws Exception {
        return null;
    }

}
