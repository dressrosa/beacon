package com.xiaoyu.test.api.impl;

import com.xiaoyu.test.api.IUserService;

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
