/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.core.common.message;

/**
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class RpcResponse extends RpcMessage {

    private Object result;

    /**
     * 当provider抛出异常,在consumer无法找到该异常类,导致无法反序列化而不知道具体的异常信息
     * 这里记录每次异常的信息.
     * 注意:不能放在字段exception后面,否则(Protostuff)反序列化(按字段顺序)的时候,如果忽略反序列化exception,
     * 则会使得errorMessage反序列化异常
     */
    private String errorMessage;

    private Throwable exception;

    public String getErrorMessage() {
        return errorMessage;
    }

    public RpcResponse setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public Throwable getException() {
        return exception;
    }

    public RpcResponse setException(Throwable exception) {
        this.exception = exception;
        this.doHandleException();
        return this;
    }

    public Object getResult() {
        return result;
    }

    public RpcResponse setResult(Object result) {
        this.result = result;
        return this;
    }

    private void doHandleException() {
        Throwable cause = this.exception;
        StringBuilder sb = new StringBuilder();
        while (cause != null) {
            sb.append(cause.getMessage());
            cause = cause.getCause();
        }
        this.errorMessage = sb.toString();
    }
}
