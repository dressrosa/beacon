package com.xiaoyu.test.api;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class HelloServiceImpl implements IHelloService {

    @Override
    public String hello(String name) {
        try {
            TimeUnit.MILLISECONDS.sleep(new Random().nextInt(1000));//
        } catch (InterruptedException e) {
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
