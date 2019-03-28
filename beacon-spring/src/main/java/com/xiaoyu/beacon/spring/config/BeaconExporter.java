/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.spring.config;

import com.xiaoyu.beacon.common.bean.BeaconBean;

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

    /**
     * 服务分组
     */
    private String group="";

    public String getGroup() {
        return group;
    }

    public BeaconExporter setGroup(String group) {
        this.group = group;
        return this;
    }

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
