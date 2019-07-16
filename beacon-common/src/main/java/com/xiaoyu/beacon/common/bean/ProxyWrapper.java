/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.beacon.common.bean;

import java.util.Map;

/**
 * 需代理对象,用于泛型调用
 * 
 * @author hongyu
 * @date 2018-07
 * @description
 */
public class ProxyWrapper {

    /**
     * 代理对象
     */
    private Object target;

    /**
     * 泛型
     */
    private boolean generic;

    /**
     * 泛型下实际调用的接口
     */
    private String realRef;

    /**
     * 泛型所提供的额外属性,如tolerant,timeout等
     */
    private Map<String, Object> attach;

    public ProxyWrapper(Object target) {
        this.target = target;
    }

    public Map<String, Object> getAttach() {
        return attach;
    }

    public ProxyWrapper setAttach(Map<String, Object> attach) {
        this.attach = attach;
        return this;
    }

    public Object getTarget() {
        return target;
    }

    public ProxyWrapper setTarget(Object target) {
        this.target = target;
        return this;
    }

    public boolean isGeneric() {
        return generic;
    }

    public ProxyWrapper setGeneric(boolean generic) {
        this.generic = generic;
        return this;
    }

    public String getRealRef() {
        return realRef;
    }

    public ProxyWrapper setRealRef(String realRef) {
        this.realRef = realRef;
        return this;
    }

}
