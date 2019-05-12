package com.xiaoyu.beacon.rpc.service;

/**
 * 泛型接口
 * 
 * @author hongyu
 * @date 2018-07
 * @description
 */
public interface GenericService {

    /**
     * @param method
     *            需要调用的方法
     * @param returnType
     *            返回类型
     * @param args
     *            参数
     * @return
     */
    public Object $_$invoke(String method, Object returnType, Object[] args);
}
