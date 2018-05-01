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
    // service->serviceDetail
    private static final ConcurrentMap<String, String> SERVICE_MAP = new ConcurrentHashMap<>(32);

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
        // server端监听client,client监听server端的
        IZkChildListener listener = new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                LOG.warn("service changed.parentPath->{},currentChilds->{}", parentPath, currentChilds);
                int size = currentChilds.size();
                // client有变化
                if (parentPath.endsWith(CONSUMERS)) {
                    if (size == 0) {
                        // client掉了
                        LOG.info("one client shutdown");
                    } else {
                        // 有client上线
                        LOG.info("new client online.");
                        doResolveInfo(currentChilds.get(0));
                    }
                } else {
                    if (size == 0) {
                        // server掉了
                        LOG.info("one server shutdown");
                        notifyChildListenr(parentPath, currentChilds);
                    } else {
                        // 有server上线
                        LOG.info("new server online.");
                        doResolveInfo(currentChilds.get(0));
                    }

                }
            }
        };
        // 初始化service父节点
        if (side.equals(From.CLIENT)) {
            // reference
            if (!this.discoverService(service)) {
                LOG.error("cannot find the service->{} in zookeeper,please check.", service);
                return;
            }
            path = ROOT + zooService + CONSUMERS;
            zoo.createPersistent(path);
            zoo.createEphemeral(path + detailInfo);
            zoo.subscribeChildChanges(ROOT + zooService + PROVIDERS, listener);
        } else {
            // exporter
            path = ROOT + zooService + PROVIDERS;
            zoo.createPersistent(path);
            zoo.createEphemeral(path + detailInfo);
            SERVICE_MAP.putIfAbsent(service, path + detailInfo);
            zoo.subscribeChildChanges(ROOT + zooService + CONSUMERS, listener);
        }
        LOG.warn("register service to zookeeper->{}", (path + detailInfo));
        CHILD_LISTENER_MAP.putIfAbsent(path, listener);
    }

    // 解析出必要的信息
    private void doResolveInfo(String path) {
        // TODO Auto-generated method stub

    }

    /**
     * 删除不用的service
     * 
     * @param parentPath
     * @param currentChilds
     */
    private void notifyChildListenr(String parentPath, List<String> currentChilds) {
        // /beancon/xxxx
        String service = parentPath.substring(7);
        SERVICE_MAP.remove(service);
        LOG.info("service->{} is shutdown.", service);
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
    public void unregisterService(String service, From side) {
        // 这里是主动取消注册.比如有个server挂了,那么从本地缓存中去除这个server
        String path = ROOT + "/" + service;
        if (side == From.CLIENT) {
            
        } else {
            
        }
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
        try {
            zoo = ZooUtil.zoo(addr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        zoo.createPersistent(ROOT);
    }

    @Override
    public boolean isInit() {
        ZooUtil tzoo = zoo;
        return tzoo == null ? false : true;
    }
}
