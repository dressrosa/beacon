/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.test.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xiaoyu.test.api.IUserService;

/**
 * @author hongyu
 * @date 2018-06
 * @description
 */
@RestController
public class BizController {

    @Autowired
    private IUserService userService;

    //for beacon
    @RequestMapping(value = "/hello", produces = "application/json;charset=UTF-8")
    public String hello(String name) {
        int re = this.userService.age(name);
        return re+"";
    }
}
