/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.transport.api;

/**
 * @author hongyu
 * @date 2018-05
 * @description
 */
public interface BeaconSide {

    /**
     * 关闭连接
     */
    public void stop();

    /**
     * 启动连接
     */
    public void start();
}
