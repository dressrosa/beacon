/**
 * 
 */
package com.xiaoyu.test.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.xiaoyu.test.api.IHelloService;

/**
 * @author hongyu
 * @date 2018-06
 * @description
 */
@RestController
public class BizController {

    @Autowired
    private IHelloService helloService;

    @RequestMapping(value = "/hello", produces = "application/json;charset=UTF-8")
    public String hello(String name) {
        System.out.println(this.helloService.toString());
        System.out.println(this.helloService.hashCode());
        String re = this.helloService.hello(name);
        return JSON.toJSONString(re);
    }
}
