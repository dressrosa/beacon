/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.cluster.loadbalance;

import java.util.List;

import com.xiaoyu.beacon.cluster.LoadBalance;
import com.xiaoyu.beacon.common.utils.IdUtil;

/**
 * 随机数法
 * 
 * @author hongyu
 * @date 2018-05
 * @description
 */
public class RandomLoadBalance implements LoadBalance {

    @Override
    public <T> T select(final List<T> providers) {
        int size = providers.size();
        if (size == 1) {
            return providers.get(0);
        }
        return providers.get(IdUtil.randomNum(size));
    }
}
