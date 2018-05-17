package com.xiaoyu.spring.config;

import com.xiaoyu.core.common.bean.BeaconBean;

/**
 * @author hongyu
 * @date 2018-04
 * @description
 */
public class BeaconProtocol extends BeaconBean {

    private String port;

    public String getPort() {
        return port;
    }

    public BeaconProtocol setPort(String port) {
        this.port = port;
        return this;
    }

}
