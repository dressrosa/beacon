package com.xiaoyu.test.api;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xiaoyu.beacon.autoconfigure.anno.BeaconExporter;

//@Service
//@BeaconExporter(interfaceName="com.xiaoyu.test.api.IHelloService")
public class HelloServiceImpl implements IHelloService {

    @Autowired
    private IUserService userService;
    
    @Override
    public String hello(String name) {
        try {
            //new Random().nextInt(5)
           TimeUnit.MILLISECONDS.sleep(3000);//
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
