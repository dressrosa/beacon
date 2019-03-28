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
     * @param returnType
     * @param args
     * @return
     */
    public Object $_$invoke(String method, Object returnType, Object[] args);
}
