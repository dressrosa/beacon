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

    private String methodName;

    private Object returnType;

    private String interfaceName;

    private String interfaceImpl;

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

    public String getInterfaceImpl() {
        return interfaceImpl;
    }

    public RpcRequest setInterfaceImpl(String interfaceImpl) {
        this.interfaceImpl = interfaceImpl;
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
