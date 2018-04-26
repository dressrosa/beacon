/**
 * 
 */
package com.xiaoyu.core.register.zookeeper;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.I0Itec.zkclient.IZkChildListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.core.common.constant.From;
import com.xiaoyu.core.register.Registry;

/**
 * server端启动时,写入providers;
 * client启动时,写入consumers;
 * 监听器:client监听server端变化,刷新本地缓存
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class ZooRegistry implements Registry {

    private static final Logger LOG = LoggerFactory.getLogger(ZooRegistry.class);

    private static final String ROOT = "/beacon";

    private static final String PROVIDERS = "/providers";

    private static final String CONSUMERS = "/consumers";

    private ZooUtil zoo;

    private static final ConcurrentMap<String, IZkChildListener> CHILD_LISTENER_MAP = new ConcurrentHashMap<>(32);

    private static final ConcurrentMap<String, String> SERVICE_MAP = new ConcurrentHashMap<>(32);

    public ZooRegistry() {
    }

    /**
     * 格式: /beacon/service-name/consumers/service-info
     */
    @Override
    public void registerService(final String service, From side) {
        // 找到需要暴漏的service,然后写入providers信息,或者客户端启动写入consumers信息
        String zooService = "/" + service;
        String detailInfo = zooService;
        zoo.createPersistent(ROOT + zooService);
        String path = null;
        // 初始化service父节点
        if (side.equals(From.CLIENT)) {
            path = ROOT + zooService + CONSUMERS;
            zoo.createPersistent(path);
            zoo.createEphemeral(path + detailInfo);
        } else {
            path = ROOT + zooService + PROVIDERS;
            zoo.createPersistent(path);
            zoo.createEphemeral(path + detailInfo);
        }

        LOG.warn("register to zoo->{}", (path + detailInfo));
        SERVICE_MAP.putIfAbsent(service, path + detailInfo);
        IZkChildListener listener = null;
        zoo.subscribeChildChanges(path, listener = new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                LOG.warn("service changed.parentPath->{}", parentPath);
                if (parentPath.endsWith(PROVIDERS)) {
                    notifyChildListenr(currentChilds);
                }
            }
        });
        CHILD_LISTENER_MAP.putIfAbsent(path, listener);
    }

    /**
     * 删除不用的service
     * 
     * @param currentChilds
     */
    protected void notifyChildListenr(List<String> currentChilds) {

    }

    @Override
    public boolean discoverService(String service) {
        // 启动时client找到对应的providers信息,保存在本地缓存
        if (SERVICE_MAP.containsKey(service)) {
            return true;
        }
        String path = ROOT + "/" + service + PROVIDERS;
        return zoo.exists(path);
    }

    @Override
    public void unregisterService(String service) {
        // 这里是主动取消注册.比如有个server挂了,那么从本地缓存中去除这个server

    }

    @Override
    public void unregisterAllServices() {
        Iterator<String> iter = SERVICE_MAP.values().iterator();
        while (iter.hasNext()) {
            String service = iter.next();
            LOG.warn("unregister service->{}", service);
            IZkChildListener dlistner = CHILD_LISTENER_MAP.remove(service);
            zoo.unsubscribeChildChanges(service, dlistner);
            dlistner = null;
            zoo.remove(service);
        }
    }

    @Override
    public void address(String addr) {
        zoo = ZooUtil.zoo(addr);
        zoo.createPersistent(ROOT);
    }
}
