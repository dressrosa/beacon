package com.xiaoyu.test.api;

import feign.Param;
import feign.RequestLine;

public interface IUserService {

    @RequestLine("GET /hello")
    public int age(@Param("name")String name);

}
