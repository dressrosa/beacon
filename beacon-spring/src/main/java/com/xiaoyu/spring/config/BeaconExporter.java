package com.xiaoyu.spring.config;

import com.xiaoyu.core.common.bean.BeaconBean;

/**
 * @author hongyu
 * @date 2018-05
 * @description
 */
public class BeaconExporter extends BeaconBean {

    private String interfaceName;
    private String ref;

    public String getInterfaceName() {
        return interfaceName;
    }

    public BeaconExporter setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
        return this;
    }

    public String getRef() {
        return ref;
    }

    public BeaconExporter setRef(String ref) {
        this.ref = ref;
        return this;
    }

}
