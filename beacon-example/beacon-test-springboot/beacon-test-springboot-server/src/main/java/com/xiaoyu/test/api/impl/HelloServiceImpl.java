/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.test.api.impl;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import com.xiaoyu.beacon.autoconfigure.anno.BeaconExporter;
import com.xiaoyu.test.api.IHelloService;

@Service
@BeaconExporter(interfaceName = "com.xiaoyu.test.api.IHelloService", group = "dev")
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HelloServiceImpl implements IHelloService {

    @Override
    public String hello(String name) {
        try {
            TimeUnit.MILLISECONDS.sleep(new Random().nextInt(500));
        } catch (Exception e) {
            e.printStackTrace();
        }
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

}
