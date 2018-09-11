/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.test.api;

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

    @RequestLine("GET /age")
    public int age(@Param("name") String name);

}
