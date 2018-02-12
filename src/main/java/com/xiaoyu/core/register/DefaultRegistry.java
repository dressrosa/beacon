/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.core.register;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.xiaoyu.core.register.zookeeper.ZooRegistry;

/**spi获取默认注册中心
 * @author hongyu
 * @date 2018-02-02
 * @description
 */
@Deprecated
public class DefaultRegistry {

    private static Registry registry;

    public static Registry getRegistry() {
        ServiceLoader<Registry> loader = ServiceLoader.load(Registry.class);
        Iterator<Registry> iter = loader.iterator();
        while (registry == null && iter.hasNext()) {
            registry = iter.next();
        }
        if (registry == null) {
            registry = new ZooRegistry();
        }
        return registry;
    }

}
