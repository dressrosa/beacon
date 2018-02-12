package com.xiaoyu.test.api;

public class HelloServiceImpl implements IHelloService {

    @Override
    public String hello(String name) {
        // try {
        // TimeUnit.MILLISECONDS.sleep(500);
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
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
