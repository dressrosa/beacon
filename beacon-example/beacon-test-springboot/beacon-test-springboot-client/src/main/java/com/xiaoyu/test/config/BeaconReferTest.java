/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.test.config;

import java.util.ArrayList;
import java.util.List;

import com.xiaoyu.beacon.spring.config.BeaconReference;
import com.xiaoyu.beacon.starter.BeaconReferConfiguration;
import com.xiaoyu.beacon.starter.anno.BeaconRefer;
import com.xiaoyu.test.api.IHelloService;
import com.xiaoyu.test.api.IUserService;

@BeaconRefer
public class BeaconReferTest extends BeaconReferConfiguration {

    @Override
    protected List<BeaconReference> doFindBeaconRefers() throws Exception {
        List<BeaconReference> list = new ArrayList<>(2);
        list.add(new BeaconReference()
                .setInterfaceName(IHelloService.class.getName())
                .setCheck(false)
                .setGroup("dev")
                .setDowngrade("fault:5"));
        list.add(new BeaconReference()
                .setInterfaceName(IUserService.class.getName())
                .setCheck(false)
                .setGroup("dev")
                .setDowngrade("limit:300"));
        return list;
    }

}
