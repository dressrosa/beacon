package com.xiaoyu.spring.config;

import com.xiaoyu.core.rpc.config.bean.BeaconBean;

public class BeaconReference extends BeaconBean {

    private String interfaceName;

    public String getInterfaceName() {
        return interfaceName;
    }

    public BeaconReference setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
        return this;
    }

}
