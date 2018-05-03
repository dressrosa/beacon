package com.xiaoyu.test.api;

public class UserServiceImpl implements IUserService {


    @Override
    public String name(String name) {
        return "you are " + name;
    }

    @Override
    public int age(String name) {
        return 15;
    }

}
