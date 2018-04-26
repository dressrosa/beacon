package com.xiaoyu.core.rpc.message;

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
