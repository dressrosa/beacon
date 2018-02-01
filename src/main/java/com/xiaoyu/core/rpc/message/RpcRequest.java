/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.core.rpc.message;

/**
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class RpcRequest extends RpcMessage {

    /**
     * 方法名
     */
    private String methodName;
    /**
     * 
     */
    private Object returnType;

    private String interfaceName;

    private Object[] params;

    public Object getReturnType() {
        return returnType;
    }

    public RpcRequest setReturnType(Object returnType) {
        this.returnType = returnType;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public RpcRequest setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public RpcRequest setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
        return this;
    }

    public Object[] getParams() {
        return params;
    }

    public RpcRequest setParams(Object[] params) {
        this.params = params;
        return this;
    }

}
