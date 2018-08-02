/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.core.rpc.api;

import com.xiaoyu.core.common.bean.ProxyWrapper;

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
