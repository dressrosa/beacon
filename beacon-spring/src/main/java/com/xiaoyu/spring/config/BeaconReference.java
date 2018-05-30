package com.xiaoyu.spring.config;

import com.xiaoyu.core.common.bean.BeaconBean;

/**
 * @author hongyu
 * @date 2018-05
 * @description
 */
public class BeaconReference extends BeaconBean {

    private String interfaceName;

    private String timeout;

    public String getTimeout() {
        return timeout;
    }

    public BeaconReference setTimeout(String timeout) {
        this.timeout = timeout;
        return this;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public BeaconReference setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
        return this;
    }

}
