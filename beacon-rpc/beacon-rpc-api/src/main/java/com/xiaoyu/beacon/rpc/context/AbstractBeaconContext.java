/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.rpc.context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.beacon.registry.Registry;
import com.xiaoyu.beacon.rpc.api.Context;
import com.xiaoyu.beacon.transport.api.Client;
import com.xiaoyu.beacon.transport.api.Server;

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

    private AtomicBoolean Started = new AtomicBoolean(false);

    private static final ReentrantLock Lock = new ReentrantLock();

    private volatile int protocolPort = 0;

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
    public Client client(String host, int serverPort) throws Exception {
        String key = host + ":" + serverPort;
        if (Client_Map.containsKey(key)) {
            return Client_Map.get(key);
        }
        /**
         * 当第一次大量请求时,可能导致client多次初始化,并覆盖掉已初始化的.
         */
        final ReentrantLock lock = Lock;
        lock.lock();
        try {
            Client client = null;
            if (!Client_Map.containsKey(key)) {
                client = this.doInitClient(host, serverPort);
                Client_Map.put(key, client);
            } else {
                client = Client_Map.get(key);
            }
            return client;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void server() throws Exception {
        if (isStarted()) {
            return;
        }
        final ReentrantLock lock = Lock;
        lock.lock();
        try {
            if (isStarted()) {
                return;
            }
            int port = this.getPort();
            if (!Server_Map.containsKey(port)) {
                Server server = this.doInitServer(port);
                Server_Map.put(port, server);
            }
        } finally {
            lock.unlock();
        }
    }

    private boolean isStarted() {
        return Started.get();
    }

    @Override
    public void start() {
        if (this.isStarted()) {
            return;
        }
        final ReentrantLock lock = Lock;
        lock.lock();
        try {
            if (this.isStarted()) {
                return;
            }
            final Map<Integer, Server> smap = Server_Map;
            if (smap != null && !smap.isEmpty()) {
                if (Started.compareAndSet(false, true)) {
                    Iterator<Server> iter = smap.values().iterator();
                    try {
                        while (iter.hasNext()) {
                            iter.next().start();
                        }
                    } catch (Exception e) {
                        // do nothing
                    }
                }
            }
        } finally {
            lock.unlock();
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
        Started.set(false);
    }

    @Override
    public void registry(Registry registry) {
        this.registry = registry;
    }

    @Override
    public Registry getRegistry() {
        return this.registry;
    }

    @Override
    public void port(int port) {
        this.protocolPort = port;
    }

    @Override
    public int getPort() {
        return this.protocolPort;
    }

    private void closeRegistry() {
        if (this.registry != null) {
            this.registry.close();
        }
    }

    public abstract Client doInitClient(String host, int port) throws Exception;

    public abstract Server doInitServer(Integer port) throws Exception;
}
