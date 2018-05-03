package com.xiaoyu.core.rpc.context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.xiaoyu.core.common.utils.IdUtil;
import com.xiaoyu.core.register.Registry;
import com.xiaoyu.transport.api.Client;
import com.xiaoyu.transport.api.Server;

/**
 * @author hongyu
 * @date 2018-04
 * @description
 */
public abstract class AbstractBeaconContext implements Context {

    protected static Map<String, Client> clientMap = new HashMap<>(16);
    protected static Map<Integer, Server> serverMap = new HashMap<>(16);

    protected Registry registry;

    private static AbstractBeaconContext abstractContext;
    protected static final String ADDRESS = "127.0.0.1";
    protected static final int PORT1 = 9111;
    protected static final int PORT2 = 9112;

    public AbstractBeaconContext() {
        abstractContext = this;
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("--------------->执行ShutdownHook<----------------");
                // do something
                // 关闭注册中心
                registry.close();
            }
        }));
    }

    @Override
    public Client client() throws Exception {
        if (clientMap.isEmpty()) {
            startClient();
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
    }

    @Override
    public void startServer() throws Exception {
        initServer();
    }

    @Override
    public void stop() {
        if (clientMap != null && !clientMap.isEmpty()) {
            Iterator<Client> iter = clientMap.values().iterator();
            while (iter.hasNext()) {
                iter.next().stop();
            }
        }
    }

    public abstract void initClient() throws Exception;

    public abstract void initServer() throws Exception;
}
