/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.cluster.faulttolerant;

import java.util.List;

import com.xiaoyu.beacon.common.bean.BeaconPath;
import com.xiaoyu.beacon.rpc.config.bean.Invocation;

/**
 * 快速失败
 * 
 * @author hongyu
 * @date 2018-05
 * @description 相当于正常调用,不做任何额外处理
 */
public class FailFastTolerant extends AbstractDefaultTolerant {

    @Override
    public Object invoke(Invocation invocation, List<BeaconPath> providers) throws Throwable {
        return super.invoke(invocation, providers);
    }

}
