/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.spring.config;

import com.xiaoyu.core.common.bean.BeaconBean;

/**
 * @author hongyu
 * @date 2018-05
 * @description 对应beacon-exporter
 */
public class BeaconExporter extends BeaconBean {

    /**
     * 接口名
     */
    private String interfaceName;
    /**
     * 实现类
     */
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
