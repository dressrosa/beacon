package com.xiaoyu.test.api.impl;

import org.springframework.stereotype.Service;

import com.xiaoyu.beacon.autoconfigure.anno.BeaconExporter;
import com.xiaoyu.test.api.IUserService;

@Service
@BeaconExporter(interfaceName = "com.xiaoyu.test.api.IUserService",group="produce")
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
