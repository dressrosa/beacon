package com.xiaoyu.core.cluster.loadbalance;

import java.util.List;
import java.util.Random;

import com.xiaoyu.core.cluster.LoadBalance;

/**
 * 随机数法
 * 
 * @author hongyu
 * @date 2018-05
 * @description
 */
public class RandomLoadBalance implements LoadBalance {

    private static final Random random = new Random();

    @Override
    public <T> T select(final List<T> providers) {
        int size = providers.size();
        if (size == 1) {
            return providers.get(0);
        }
        return providers.get(random.nextInt(size));
    }
}
