package com.xiaoyu.test.springboot.config;

import java.util.ArrayList;
import java.util.List;

import com.xiaoyu.beacon.autoconfigure.BeaconReferConfiguration;
import com.xiaoyu.beacon.autoconfigure.anno.BeaconRefer;
import com.xiaoyu.spring.config.BeaconReference;
import com.xiaoyu.test.api.IHelloService;
import com.xiaoyu.test.api.IUserService;

@BeaconRefer
public class BeaconReferTest extends BeaconReferConfiguration {

    @Override
    protected List<BeaconReference> doFindBeaconRefers() {
        List<BeaconReference> list = new ArrayList<>();
        list.add(new BeaconReference().setInterfaceName(IHelloService.class.getName()).setCheck(false));
        list.add(new BeaconReference().setInterfaceName(IUserService.class.getName()).setCheck(false));
        return list;
    }

}
