/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.rpc.api;

import com.xiaoyu.beacon.registry.Registry;
import com.xiaoyu.beacon.transport.api.Client;

/**
 * @author hongyu
 * @date 2018-04
 * @description
 */
public interface Context {

    /**
     * 返回client
     * 
     * @param host
     * @param serverPort
     * @return
     * @throws Exception
     */
    Client client(String host, int serverPort) throws Exception;

    /**
     * 设置server
     * 
     * @throws Exception
     */
    void server() throws Exception;

    /**
     * 设置registry
     * 
     * @param registry
     */
    void registry(Registry registry);

    /**
     * 返回当前context的registry
     * 
     * @return
     */
    Registry getRegistry();

    /**
     * 停止context
     */
    void shutdown();

    /**
     * 启动beacon
     */
    void start();

    void port(int port);

    int getPort();
}
