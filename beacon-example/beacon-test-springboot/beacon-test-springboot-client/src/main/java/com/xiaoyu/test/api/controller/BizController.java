/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.test.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xiaoyu.beacon.common.exception.FusedException;
import com.xiaoyu.test.api.IHelloService;
import com.xiaoyu.test.api.IUserService;

/**
 * @author hongyu
 * @date 2018-06
 * @description
 */
@RestController
public class BizController {

    @Autowired
    private IHelloService helloService;
    @Autowired
    private IUserService userService;

    // for beacon
    @RequestMapping(value = "/hello", produces = "application/json;charset=UTF-8")
    public String hello(String name) {
        String re = "";
        try {
            re = this.helloService.hello(name);
        } catch (FusedException e) {
            System.out.println("熔断补偿处理....");
        }
        return re;
    }

    @RequestMapping(value = "/age", produces = "application/json;charset=UTF-8")
    public String user(String name) {
        int re = 0;
        try {
            re = this.userService.age(name);
        } catch (FusedException e) {
           // System.out.println("熔断补偿处理....");
        }
        return re + "";
    }
}
