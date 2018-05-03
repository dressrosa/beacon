/**
 * 
 */
package com.xiaoyu.core.rpc.config.bean;

import com.xiaoyu.core.common.constant.From;

/**
 * @author hongyu
 * @date 2018-05
 * @description
 */
public class BeaconPath {

    private String service;

    private String host;

    private From side;

    public String getService() {
        return service;
    }

    public BeaconPath setService(String service) {
        this.service = service;
        return this;
    }

    public String getHost() {
        return host;
    }

    public BeaconPath setHost(String host) {
        this.host = host;
        return this;
    }

    public From getSide() {
        return side;
    }

    public BeaconPath setSide(From side) {
        this.side = side;
        return this;
    }

    public String toPath() {
        final StringBuilder builder = new StringBuilder();
        builder.append("host=").append(this.getHost())
                .append("&service=").append(this.getService())
                .append("&side=").append(this.getSide().name());
        return builder.toString();
    }

}
