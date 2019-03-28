/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.common.message;

/**
 * @author hongyu
 * @date 2018-02
 * @description 存储调用成功的结果
 */
public class CallbackListener {

    private volatile Object result = null;

    public Object result() {
        return result;
    }

    public void onSuccess(Object result) {
        this.result = result;
    }

}
