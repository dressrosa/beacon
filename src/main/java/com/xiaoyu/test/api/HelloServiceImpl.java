package com.xiaoyu.test.api;

import java.util.concurrent.TimeUnit;

public class HelloServiceImpl implements IHelloService {

    @Override
    public String hello(String name) {
         try {
         TimeUnit.MILLISECONDS.sleep(4999);
         } catch (InterruptedException e) {
         // TODO Auto-generated catch block
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
