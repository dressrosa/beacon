/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.beacon.proxy.api;

import com.xiaoyu.beacon.common.bean.ProxyWrapper;

/**
 * @author hongyu
 * @date 2017-04
 * @description
 */
public interface IProxy {

    /**
     * @param wrapper
     * @return
     */
    public Object getProxy(final ProxyWrapper wrapper);
}
