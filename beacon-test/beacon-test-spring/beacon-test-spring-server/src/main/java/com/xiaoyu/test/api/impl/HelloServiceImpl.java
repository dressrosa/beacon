/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.test.api.impl;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.xiaoyu.test.api.IHelloService;
import com.xiaoyu.test.api.IUserService;

/**
 * @author hongyu
 * @date 2018-08
 * @description
 */
public class HelloServiceImpl implements IHelloService {

    @Autowired
    private IUserService userService;

    @Override
    public String hello(String name) {
        try {
            TimeUnit.MILLISECONDS.sleep(new Random().nextInt(500));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "hello " + userService.age(name);
    }

    @Override
    public String name(String name) {
        return "you are " + name;
    }

    @Override
    public void sing(String song) {
        System.out.println("唱歌:" + song);

    }

}
