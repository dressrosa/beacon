/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.test.api.impl;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import com.xiaoyu.beacon.starter.anno.BeaconExporter;
import com.xiaoyu.test.api.IUserService;

@Service
@BeaconExporter(interfaceName = "com.xiaoyu.test.api.IUserService", group = "dev")
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserServiceImpl implements IUserService {

    @Override
    public int age(String name) {
        int time = new Random().nextInt(200);
        try {
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(name);
        return name.length();
    }

}
