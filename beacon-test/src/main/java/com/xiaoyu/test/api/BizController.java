/**
 * 
 */
package com.xiaoyu.test.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hongyu
 * @date 2018-06
 * @description
 */
@RestController
public class BizController {

    @Autowired
    private IHelloService helloService;

    @RequestMapping("/hello")
    public String hello() {
        return this.helloService.hello("xiao");
    }
}
