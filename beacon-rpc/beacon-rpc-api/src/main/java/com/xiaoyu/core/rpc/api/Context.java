/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.core.rpc.api;

import com.xiaoyu.core.register.Registry;
import com.xiaoyu.transport.api.Client;

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
     * @param port
     * @return
     * @throws Exception
     */
    public Client client(String host, int port) throws Exception;

    /**
     * 设置server
     * 
     * @param port
     * @throws Exception
     */
    public void server(int port) throws Exception;

    /**
     * 设置registry
     * 
     * @param registry
     */
    public void registry(Registry registry);

    /**
     * 返回当前context的registry
     * 
     * @return
     */
    public Registry getRegistry();

    /**
     * 停止context
     */
    public void shutdown();

    /**
     * 启动beacon
     */
    public void start();
}
