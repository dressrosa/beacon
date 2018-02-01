package com.xiaoyu.core.rpc.context;

import java.util.HashMap;
import java.util.Map;

import com.xiaoyu.core.proxy.DefaultProxy;
import com.xiaoyu.test.api.IHelloService;
import com.xiaoyu.transport.netty.NettyClient;
import com.xiaoyu.transport.netty.NettyServer;

/**for test
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconContext {

    private static Map<Class<?>, Object> proxyMap = new HashMap<>();

    private static NettyClient[] clients;
    private static NettyServer[] servers;

    public static void start() {
        initProxyMap();
        initServer();
        initClient();
    }

    public static void stop() {
        for (NettyClient c : clients) {
            c.stop();
        }
        for (NettyServer s : servers) {
            s.stop();
        }
    }

    public static Map<Class<?>, Object> getProxyMap() {
        return proxyMap;
    }

    private static void initProxyMap() {
        proxyMap.put(IHelloService.class, DefaultProxy.getProxy(IHelloService.class));
    }

    private static void initClient() {
        clients = new NettyClient[] { new NettyClient("127.0.0.1", 9090), new NettyClient("127.0.0.1", 9091) };
        for (NettyClient client : clients) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.connect();
                }
            }).start();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void initServer() {
        servers = new NettyServer[] { new NettyServer(9090), new NettyServer(9091) };

        for (NettyServer server : servers) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        server.bind();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
