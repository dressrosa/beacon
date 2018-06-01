/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.spring.config;

import com.xiaoyu.core.common.bean.BeaconBean;

/**
 * @author hongyu
 * @date 2018-05
 * @description 对应于beacon-reference
 */
public class BeaconReference extends BeaconBean {

    /**
     * 接口名
     */
    private String interfaceName;

    /**
     * 请求超时
     */
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
