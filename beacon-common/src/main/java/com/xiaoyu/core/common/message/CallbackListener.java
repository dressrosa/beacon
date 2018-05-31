/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.core.common.message;

/**
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class CallbackListener {

    private volatile Object result = null;

    public Object result() {
        Object ret = result;
        return ret;
    }

    public void onSuccess(Object result) {
        this.result = result;
    }

}
