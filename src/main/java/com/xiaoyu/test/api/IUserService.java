package com.xiaoyu.test.api;

import com.xiaoyu.core.rpc.config.anno.RpcRefer;

@RpcRefer(UserServiceImpl.class)
public interface IUserService extends IBaseService {

    public int age(String name);

}
