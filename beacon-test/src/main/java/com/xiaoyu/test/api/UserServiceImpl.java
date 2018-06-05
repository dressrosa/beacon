package com.xiaoyu.test.api;

import org.springframework.stereotype.Service;

import com.xiaoyu.beacon.autoconfigure.anno.BeaconExporter;

//@Service
//@BeaconExporter(interfaceName="com.xiaoyu.test.api.IUserService")
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
