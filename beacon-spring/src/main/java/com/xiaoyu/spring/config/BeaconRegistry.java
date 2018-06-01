/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.spring.config;

import com.xiaoyu.core.common.bean.BeaconBean;

/**
 * @author hongyu
 * @date 2018-04
 * @description 对应于beacon-registry
 */
public class BeaconRegistry extends BeaconBean {

    /**
     * 地址 格式ip:port
     */
    private String address;

    /**
     * 协议 zookeeper
     */
    private String protocol;

    /**
     * 端口,在xml中不显示
     */
    private String port;

    public String getPort() {
        return port;
    }

    public BeaconRegistry setPort(String port) {
        this.port = port;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public BeaconRegistry setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public BeaconRegistry setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

}
