/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.test.api.impl;

import org.springframework.stereotype.Service;

import com.xiaoyu.beacon.starter.anno.BeaconExporter;
import com.xiaoyu.test.api.IUserService;

@Service
@BeaconExporter(interfaceName = "com.xiaoyu.test.api.IUserService", methods = "age", group = "dev")
public class UserServiceImpl implements IUserService {

    @Override
    public int age(String name) {
        System.out.println("name:"+name);
        return 12;
    }

}
