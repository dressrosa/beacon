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
    // host->client
    protected static Map<String, Client> clientMap = new HashMap<>(16);
    // port->server
    protected static Map<Integer, Server> serverMap = new HashMap<>(16);

    protected Registry registry;

    private static AbstractBeaconContext abstractContext;

    private static final ReentrantLock clientLock = new ReentrantLock();

    public AbstractBeaconContext() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                // do something
                LOG.info("-do something with the shutdownhook-");
                // 等service取消注册后才关闭注册中心
                abstractContext.closeRegistry();
            }
        }));
        abstractContext = this;
    }

    @Override
    public Client client(String host, int port) throws Exception {
        clientLock.lock();
        try {
            if (clientMap.containsKey(host)) {
                return clientMap.get(host);
            }
            Client client = doInitClient(host, port);
            clientMap.put(host, client);
            return client;
        } finally {
            clientLock.unlock();
        }

    }

    @Override
    public void server(int port) throws Exception {
        Server server = doInitServer(port);
        serverMap.put(port, server);
    }

    @Override
    public void stop() {
        doCloseClient();
    }

    private void doCloseClient() {
        if (clientMap != null && !clientMap.isEmpty()) {
            Iterator<Client> iter = clientMap.values().iterator();
            try {
                while (iter.hasNext()) {
                    iter.next().stop();
                }
            } catch (Exception e) {
                // do nothing
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
