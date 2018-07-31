/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.filter.generic;

import com.xiaoyu.core.common.constant.From;

/**
 * @author hongyu
 * @date 2018-07
 * @description 对应于BeaconReference,仅用于泛型调用
 */
public class GenericReference {

    private String from = From.CLIENT.name();
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
    private boolean check = false;

    /**
     * 容错策略
     */
    private String tolerant = "failfast";

    /**
     * 是否泛型接口
     */
    private boolean generic = false;

    public String getFrom() {
        return from;
    }

    public GenericReference setFrom(String from) {
        this.from = from;
        return this;
    }

    public boolean isGeneric() {
        return generic;
    }

    public GenericReference setGeneric(boolean generic) {
        this.generic = generic;
        return this;
    }

    public String getTolerant() {
        return tolerant;
    }

    public void setTolerant(String tolerant) {
        this.tolerant = tolerant;
    }

    public boolean getCheck() {
        return check;
    }

    public GenericReference setCheck(boolean check) {
        this.check = check;
        return this;
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
