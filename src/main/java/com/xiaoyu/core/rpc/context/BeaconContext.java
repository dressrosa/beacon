package com.xiaoyu.core.rpc.context;

import com.xiaoyu.core.register.Registry;
import com.xiaoyu.transport.api.Client;
import com.xiaoyu.transport.api.Server;
import com.xiaoyu.transport.netty.NettyClient;
import com.xiaoyu.transport.netty.NettyServer;

/**
 * @author hongyu
 * @date 2018-04
 * @description
 */
public class BeaconContext extends AbstractBeaconContext {

    @Override
    public void initClient() throws Exception {
        Client client1 = new NettyClient(ADDRESS, PORT1);
        Client client2 = new NettyClient(ADDRESS, PORT2);
        clientMap.put(ADDRESS + PORT1, client1);
        clientMap.put(ADDRESS + PORT2, client2);
        client1.start();
        client2.start();
    }

    @Override
    public void initServer() throws Exception {
        Server server1 = new NettyServer(PORT1);
        Server server2 = new NettyServer(PORT2);
        serverMap.put(PORT1, server1);
        serverMap.put(PORT2, server2);
        server1.start();
        server2.start();
    }

    @Override
    public void registry(Registry registry) {
        this.registry = registry;

    }

}
