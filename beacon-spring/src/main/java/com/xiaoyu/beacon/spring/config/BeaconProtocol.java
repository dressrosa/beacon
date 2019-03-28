/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.spring.config;

import com.xiaoyu.beacon.common.bean.BeaconBean;

/**
 * @author hongyu
 * @date 2018-04
 * @description 对应与beacon-protocol
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
