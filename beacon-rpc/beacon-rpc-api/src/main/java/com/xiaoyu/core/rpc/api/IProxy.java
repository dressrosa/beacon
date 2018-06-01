/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.core.rpc.api;

/**
 * @author hongyu
 * @date 2017-04
 * @description
 */
public interface IProxy {

    /**
     * @param target
     * @return
     */
    public Object getProxy(final Object target);

}
