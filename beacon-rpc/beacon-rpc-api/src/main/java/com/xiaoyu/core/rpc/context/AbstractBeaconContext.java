/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.core.rpc.context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.core.register.Registry;
import com.xiaoyu.core.rpc.api.Context;
import com.xiaoyu.transport.api.Client;
import com.xiaoyu.transport.api.Server;

/**
 * @author hongyu
 * @date 2018-04
 * @description 抽象context,完成client server的设置
 */
public abstract class AbstractBeaconContext implements Context {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractBeaconContext.class);

    // host+":"+port->client
    protected static Map<String, Client> Client_Map = new HashMap<>(16);
    // port->server
    protected static Map<Integer, Server> Server_Map = new HashMap<>(16);

    protected Registry registry;

    private static final ReentrantLock Client_Lock = new ReentrantLock();

    public AbstractBeaconContext() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                // do something
                LOG.info("-do something with the shutdownhook-");
                // 等service取消注册后才关闭注册中心
                closeRegistry();
            }
        }, "beacon-shutdown-hook"));
    }

    @Override
    public Client client(String host, int port) throws Exception {
        String key = host + ":" + port;
        if (Client_Map.containsKey(key)) {
            return Client_Map.get(key);
        }
        /**
         * 当第一次大量请求时,可能导致client多次初始化,并覆盖掉已初始化的.
         */
        Client_Lock.lock();
        try {
            Client client = this.doInitClient(host, port);
            Client_Map.put(key, client);
            return client;
        } finally {
            Client_Lock.unlock();
        }
    }

    @Override
    public void server(int port) throws Exception {
        Server server = this.doInitServer(port);
        Server_Map.put(port, server);
    }

    @Override
    public void start() {
        if (Server_Map != null && !Server_Map.isEmpty()) {
            Iterator<Server> iter = Server_Map.values().iterator();
            try {
                while (iter.hasNext()) {
                    iter.next().start();
                }
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    @Override
    public void shutdown() {
        LOG.info("Begin shutdown beacon.");
        this.doShutdown();
        LOG.info("Completely shutdown beacon.");
    }

    private void doShutdown() {
        if (Client_Map != null && !Client_Map.isEmpty()) {
            Iterator<Client> iter = Client_Map.values().iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().stop();
                } catch (Exception e) {
                    // do nothing
                }
            }
        }
        if (Server_Map != null && !Server_Map.isEmpty()) {
            Iterator<Server> iter = Server_Map.values().iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().stop();
                } catch (Exception e) {
                    // do nothing
                }
            }
        }
    }

    @Override
    public void registry(Registry registry) {
        this.registry = registry;
    }

    @Override
    public Registry getRegistry() {
        return this.registry;
    }

    private void closeRegistry() {
        this.registry.close();
    }

    public abstract Client doInitClient(String host, int port) throws Exception;

    public abstract Server doInitServer(Integer port) throws Exception;
}
