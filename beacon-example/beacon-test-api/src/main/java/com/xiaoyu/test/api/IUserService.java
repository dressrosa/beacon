/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.test.api;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

/**
 * for http
 * 
 * @author hongyu
 * @date 2018-08
 * @description
 */
public interface IUserService {

    // @RequestLine("GET /age?name={name}")
    // public int age(@Param("name") String name);
    @RequestLine("POST /age")
    @Headers("Content-Type: application/json")
    public int age(@Param("name") String name);

}
