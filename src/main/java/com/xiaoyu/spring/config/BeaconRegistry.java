package com.xiaoyu.spring.config;

import com.xiaoyu.core.rpc.config.bean.BeaconBean;

/**
 * @author hongyu
 * @date 2018-04
 * @description
 */
public class BeaconRegistry extends BeaconBean {

    private String address;

    private String protocol;

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
