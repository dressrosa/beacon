/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.core.rpc.message;

/**
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class RpcResponse extends RpcMessage {

    @Override
    public String toString() {
        return "ResponseMsg []" + getId();
    }

    private Object result;

    public Object getResult() {
        return result;
    }

    public RpcResponse setResult(Object result) {
        this.result = result;
        return this;
    }

}