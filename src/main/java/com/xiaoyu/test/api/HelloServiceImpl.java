package com.xiaoyu.test.api;

public class HelloServiceImpl implements IHelloService {

    @Override
    public String hello(String name) {
        return "hello " + name;
    }

}
