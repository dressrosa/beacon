package com.xiaoyu.core.rpc.context;

import java.util.HashMap;
import java.util.Map;

import com.xiaoyu.core.common.constant.From;
import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.common.utils.IdUtil;
import com.xiaoyu.core.proxy.IProxy;
import com.xiaoyu.core.register.Registry;
import com.xiaoyu.test.api.IHelloService;
import com.xiaoyu.transport.netty.NettyClient;
import com.xiaoyu.transport.netty.NettyServer;

/**
 * for test
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconTestContext {

    private static Map<Class<?>, Object> proxyMap = new HashMap<>(16);

    private static NettyClient[] clients;
    private static NettyServer[] servers;

    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT1 = 9111;
    private static final int PORT2 = 9112;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                BeaconTestContext.stop();
            }
        }));
    }

    public static NettyClient client() throws Exception {
        if (clients == null) {
            startServer();
            startClient();
            // throw new Exception("no server start,please check.");
        }
        if (clients.length == 1) {
            return clients[0];
        } else {
            return clients[IdUtil.randomNum(clients.length)];
        }
    }

    public static void startClient() throws Exception {
        initProxyMap();
        initClient();
        exportService(From.CLIENT);

    }

    public static void startServer() throws Exception {
        initProxyMap();
        initServer();

        exportService(From.SERVER);
    }

    public static void stop() {
        unregisterService();
        if (clients != null && clients.length > 0) {
            for (NettyClient c : clients) {
                c.stop();
            }
        }
        // if (servers != null && servers.length > 0) {
        // for (NettyServer s : servers) {
        // s.stop();
        // }
        // }

    }

    public static Object getBean(Class<?> cls) {
        return proxyMap.get(cls);
    }

    /**
     * 实际这里是由spring加载bean
     */
    private static void initProxyMap() {
        try {
            proxyMap.put(IHelloService.class, SpiManager.defaultSpiExtender(IProxy.class)
                    .getProxy(IHelloService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 加载bean的同时,server:将service存入zoo, client:写入消费者信息
        // 写入本地缓存,由监听器监听server和client的情况,然后更新本地缓存
    }

    private static void exportService(From side) {
        Registry reg = null;
        try {
            reg = SpiManager.defaultSpiExtender(Registry.class);
            reg.address("127.0.0.1");
            reg.registerService("com.xiaoyu.test.api.IHelloService", side);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void unregisterService() {
        Registry reg = null;
        try {
            reg = SpiManager.defaultSpiExtender(Registry.class);
            reg.unregisterAllServices();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initClient() throws Exception {
        clients = new NettyClient[] { new NettyClient(ADDRESS, PORT1), new NettyClient(ADDRESS, PORT2) };
        for (NettyClient client : clients) {
            client.start();
        }
    }

    private static void initServer() throws Exception {
        servers = new NettyServer[] { new NettyServer(PORT1), new NettyServer(PORT2) };
        for (NettyServer server : servers) {
            server.start();
        }
    }
}
