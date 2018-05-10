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

    private String ref;

    private String host;

    private String port;

    private From side;

    public String getService() {
        return service;
    }

    public BeaconPath setService(String service) {
        this.service = service;
        return this;
    }

    public String getRef() {
        return ref == null ? "" : ref;
    }

    public BeaconPath setRef(String ref) {
        this.ref = ref;
        return this;
    }

    public String getHost() {
        return host;
    }

    public BeaconPath setHost(String host) {
        this.host = host;
        return this;
    }

    public String getPort() {
        return port;
    }

    public BeaconPath setPort(String port) {
        this.port = port;
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
        if (this.getSide() == From.SERVER) {
            builder.append("host=").append(this.getHost() + ":" + this.getPort());
        } else {
            builder.append("host=").append(this.getHost());
        }

        builder.append("&service=").append(this.getService())
                .append("&ref=").append(this.getRef())
                .append("&side=").append(this.getSide().name());
        return builder.toString();
    }

    public static BeaconPath toEntity(String path) {
        // [host=192.168.61.239:1992&service=com.xxx.IHelloService&ref=com.xxx.HelloServiceImpl&side=SERVER]
        BeaconPath bea = new BeaconPath();
        String[] arr1 = path.split("&");
        for (String str : arr1) {
            if (str.startsWith("host")) {
                String arr[] = str.substring(5).split(":");
                if (arr.length == 1) {
                    bea.setHost(arr[0]);
                } else {
                    bea.setHost(arr[0]);
                    bea.setPort(arr[1]);
                }
            } else if (str.startsWith("service")) {
                bea.setService(str.substring(8));
            } else if (str.startsWith("ref")) {
                bea.setRef(str.substring(4));
            } else if (str.startsWith("side")) {
                bea.setSide(From.fromName(str.substring(5)));
            }
        }
        return bea;
    }

}
