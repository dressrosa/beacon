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
    private String timeout = "3000";

    /**
     * 重试次数
     */
    private Integer retry = 0;

    /**
     * 启动时检查
     */
    private boolean check;

    /**
     * 容错策略
     */
    private String tolerant = "failfast";

    /**
     * 是否泛型接口
     */
    private boolean generic = false;

    /**
     * 服务分组
     */
    private String group = "";

    /**
     * 服务降级 格式(策略:降级类)
     * timeout:null
     * timeout:xxx
     * retry:xxx
     * fault:xxx
     * limit:xxx
     */
    private String downgrade;

    public String getDowngrade() {
        return downgrade;
    }

    public BeaconReference setDowngrade(String downgrade) {
        this.downgrade = downgrade;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public BeaconReference setGroup(String group) {
        this.group = group;
        return this;
    }

    public boolean isGeneric() {
        return generic;
    }

    public BeaconReference setGeneric(boolean generic) {
        this.generic = generic;
        return this;
    }

    public String getTolerant() {
        return tolerant;
    }

    public BeaconReference setTolerant(String tolerant) {
        this.tolerant = tolerant;
        return this;
    }

    public boolean getCheck() {
        return check;
    }

    public BeaconReference setCheck(boolean check) {
        this.check = check;
        return this;
    }

    public Integer getRetry() {
        return retry;
    }

    public BeaconReference setRetry(Integer retry) {
        this.retry = retry;
        return this;
    }

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
