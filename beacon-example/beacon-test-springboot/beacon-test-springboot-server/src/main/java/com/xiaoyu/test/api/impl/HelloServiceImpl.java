/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.test.api.impl;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import com.xiaoyu.beacon.starter.anno.BeaconExporter;
import com.xiaoyu.test.api.IHelloService;

@Service
@BeaconExporter(interfaceName = "com.xiaoyu.test.api.IHelloService", group = "dev")
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HelloServiceImpl implements IHelloService {

    @Override
    public String hello(String name) {
        int time = new Random().nextInt(500);
        if (time > 450) {
            int a = 0;
            int b = 10;
            System.out.println(b / a);
        }
        try {
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("hello " + name);
        return "hello " + name;
    }

    @Override
    public String name(String name) {
        return "you are " + name;
    }

    @Override
    public void sing(String song) {
        System.out.println("唱歌:" + song);

    }

    @Override
    public int age(String name) {
        return 17;
    }

}
