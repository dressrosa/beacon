/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.test.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xiaoyu.core.common.exception.FusedException;
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

    // for beacon
    @RequestMapping(value = "/hello", produces = "application/json;charset=UTF-8")
    public String hello(String name) {
        String re = "";
        try {
            re = this.helloService.hello(name);
        } catch (FusedException e) {
            System.out.print("熔断补偿处理....");
        }
        return re;
    }
}
