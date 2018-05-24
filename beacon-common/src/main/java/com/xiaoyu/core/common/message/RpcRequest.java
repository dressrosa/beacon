/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.core.common.message;

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
     * 返回类型
     */
    private Object returnType;

    /**
     * 接口名
     */
    private String interfaceName;

    /**
     * 接口实现
     */
    private String interfaceImpl;

    /**
     * 参数
     */
    private Object[] params;

    /**
     * 请求超时
     */
    private long timeout;

    public long getTimeout() {
        return timeout;
    }

    public RpcRequest setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

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
