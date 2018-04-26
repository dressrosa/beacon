package com.xiaoyu.core.rpc.context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.xiaoyu.core.common.constant.From;
import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.common.utils.IdUtil;
import com.xiaoyu.core.register.Registry;
import com.xiaoyu.transport.api.Client;
import com.xiaoyu.transport.api.Server;

/**
 * @author hongyu
 * @date 2018-04
 * @description
 */
public abstract class AbstractBeaconContext implements Context{

    // protected static Client[] clientMap;
    // protected static Server[] servers;

    protected static Map<String, Client> clientMap = new HashMap<>(16);
    protected static Map<Integer, Server> serverMap = new HashMap<>(16);

    protected static final String ADDRESS = "127.0.0.1";
    protected static final int PORT1 = 9111;
    protected static final int PORT2 = 9112;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("是否执行...");
                AbstractBeaconContext.stop();
            }
        }));
    }

    @Override
    public Client client() throws Exception {
        if (clientMap.isEmpty()) {
            startServer();
            startClient();
            // throw new Exception("no server start,please check.");
        }
        Client[] clients = clientMap.values().toArray(new Client[0]);
        if (clientMap.size() == 1) {
            return clients[0];
        } else {
            return clients[IdUtil.randomNum(clientMap.size())];
        }
    }

    public void startClient() throws Exception {
        initClient();
        exportService(From.CLIENT);

    }

    public void startServer() throws Exception {
        initServer();
        exportService(From.SERVER);
    }

    public static void stop() {
        unregisterService();
        if (clientMap != null && !clientMap.isEmpty()) {
            Iterator<Client> iter = clientMap.values().iterator();
            while (iter.hasNext()) {
                iter.next().stop();
            }
        }
        // if (servers != null && servers.length > 0) {
        // for (NettyServer s : servers) {
        // s.stop();
        // }
        // }
    }

    private static void exportService(From side) {
        Registry reg = null;
        try {
            reg = SpiManager.defaultSpiExtender(Registry.class);
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

    public abstract void initClient() throws Exception;

    public abstract void initServer() throws Exception;
}
