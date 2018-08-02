package com.xiaoyu.test.api.impl;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xiaoyu.beacon.autoconfigure.anno.BeaconExporter;
import com.xiaoyu.test.api.IHelloService;
import com.xiaoyu.test.api.IUserService;

@Service
@BeaconExporter(interfaceName = "com.xiaoyu.test.api.IHelloService",methods="hello,name,sing",group="dev")
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
        return "hello " + userService.name(name);
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
