/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.beacon.proxy.common;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.xiaoyu.beacon.common.bean.ProxyWrapper;
import com.xiaoyu.beacon.common.extension.SpiManager;
import com.xiaoyu.beacon.common.generic.GenericReference;
import com.xiaoyu.beacon.common.utils.StringUtil;
import com.xiaoyu.beacon.proxy.api.IProxy;
import com.xiaoyu.beacon.rpc.service.GenericService;

/**
 * 泛型调用服务发布
 * 
 * @author hongyu
 * @date 2018-07
 * @description
 */
public class GenericRequestLauncher {

    /**
     * key->proxy
     */
    private static final WeakHashMap<String, Object> Ref_Map = new WeakHashMap<>(16);

    private static final Lock Put_Lock = new ReentrantLock();

    @SuppressWarnings("unchecked")
    public static <T> T launch(GenericReference ref) throws Exception {
        if (ref == null) {
            return null;
        }
        if (StringUtil.isEmpty(ref.getInterfaceName())) {
            throw new RuntimeException("InterfaceName should be provided");
        }
        String key = ref.toString();
        final WeakHashMap<String, Object> refMap = Ref_Map;
        if (!refMap.containsKey(key)) {
            IProxy proxy = SpiManager.defaultSpiExtender(IProxy.class);
            ProxyWrapper wrapper = new ProxyWrapper(GenericService.class)
                    .setGeneric(true)
                    .setRealRef(ref.getInterfaceName());
            Map<String, Object> attach = new HashMap<>(4);
            attach.put("tolerant", ref.getTolerant());
            attach.put("timeout", ref.getTimeout());
            attach.put("group", ref.getGroup() == null ? "" : ref.getGroup());
            attach.put("host", ref.getHost() == null ? "" : ref.getHost());
            wrapper.setAttach(attach);
            // safe concurrent
            final Lock lock = Put_Lock;
            lock.lock();
            try {
                if (!refMap.containsKey(key)) {
                    refMap.put(key, proxy.getProxy(wrapper));
                }
            } finally {
                lock.unlock();
            }
        }
        return (T) refMap.get(key);
    }
}
