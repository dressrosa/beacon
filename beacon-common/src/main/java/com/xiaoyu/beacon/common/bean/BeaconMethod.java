package com.xiaoyu.beacon.common.bean;

/**
 * @author hongyu
 * @date 2019-10
 * @description 方法级限制
 */
public class BeaconMethod {

    private String methodName;
    private Integer timeout;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

}