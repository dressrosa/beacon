/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.core.proxy;

/**
 * 2017年4月20日下午2:44:00
 * 
 * @author xiaoyu
 * @description
 */
public interface IProxy {

    /**
     * @param target
     * @return
     */
    public Object getProxy(final Object target);

}
