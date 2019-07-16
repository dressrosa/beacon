/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.rpc.beacon.context;

import com.xiaoyu.beacon.rpc.context.AbstractBeaconContext;
import com.xiaoyu.beacon.transport.api.Client;
import com.xiaoyu.beacon.transport.api.Server;
import com.xiaoyu.beacon.transport.netty.NettyClient;
import com.xiaoyu.beacon.transport.netty.NettyServer;

/**
 * @author hongyu
 * @date 2018-04
 * @description
 */
public class BeaconContext extends AbstractBeaconContext {

    @Override
    public Client doInitClient(String host, int port) throws Exception {
        final Client client = new NettyClient(host, port);
        client.start();
        return client;
    }

    @Override
    public Server doInitServer(Integer port) throws Exception {
        final Server server = new NettyServer(port);
        return server;
    }

}
