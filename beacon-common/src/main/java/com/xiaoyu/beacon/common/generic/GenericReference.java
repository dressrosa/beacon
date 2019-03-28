/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.common.generic;

/**
 * @author hongyu
 * @date 2018-07
 * @description 对应于BeaconReference,仅用于泛型调用
 */
/**
 * @author hongyu
 * @date 2018-08
 * @description
 */
public class GenericReference {

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
     * 容错策略
     */
    private String tolerant = "failfast";

    /**
     * 服务分组
     */
    private String group = "";

    public String getGroup() {
        return group;
    }

    public GenericReference setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getTolerant() {
        return tolerant;
    }

    public void setTolerant(String tolerant) {
        this.tolerant = tolerant;
    }

    public Integer getRetry() {
        return retry;
    }

    public GenericReference setRetry(Integer retry) {
        this.retry = retry;
        return this;
    }

    public String getTimeout() {
        return timeout;
    }

    public GenericReference setTimeout(String timeout) {
        this.timeout = timeout;
        return this;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public GenericReference setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
        return this;
    }

}
