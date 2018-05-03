/**
 * 
 */
package com.xiaoyu.core.register.zookeeper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.I0Itec.zkclient.IZkChildListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.core.common.constant.From;
import com.xiaoyu.core.register.AbstractRegistry;
import com.xiaoyu.core.rpc.config.bean.BeaconPath;

/**
 * server端启动时,写入providers;
 * client启动时,写入consumers;
 * 监听器:client监听server端变化,刷新本地缓存
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class ZooRegistry extends AbstractRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(ZooRegistry.class);

    private static final String ROOT = "/beacon";

    private static final String PROVIDERS = "/providers";

    private static final String CONSUMERS = "/consumers";

    private ZooUtil zoo;

    private static final ConcurrentMap<String, IZkChildListener> CHILD_LISTENER_MAP = new ConcurrentHashMap<>(32);

    /**
     * 格式: /beacon/service-name/consumers/service-info
     */
    @Override
    public void registerService(BeaconPath beaconPath) {
        // 找到需要暴漏的service,然后写入providers信息,或者客户端启动写入consumers信息
        String detailInfo = beaconPath.toPath();
        String service = beaconPath.getService();
        zoo.createPersistent(ROOT + "/" + service);
        String path = null;
        // server端监听client,client监听server端的
        IZkChildListener listener = new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                LOG.warn("service changed.parentPath->{},currentChilds->{}", parentPath, currentChilds);
                int size = currentChilds.size();
                // client有变化
                if (parentPath.endsWith(CONSUMERS)) {
                    doResolveInfo(parentPath, currentChilds);
                } else {
                    if (size == 0) {
                        // server掉了
                        LOG.info("one server shutdown");
                        notifyChildListenr(parentPath, currentChilds);
                    } else {
                        // 有server上线
                        LOG.info("new server online.");
                    }

                }
            }
        };
        // 初始化service父节点
        if (beaconPath.getSide().equals(From.CLIENT)) {
            // reference
            if (!this.discoverService(beaconPath.getService())) {
                LOG.error("cannot find the service->{} in zookeeper,please check.", beaconPath.getService());
                return;
            }
            path = ROOT + "/" + service + CONSUMERS;
            zoo.createPersistent(path);
            zoo.createEphemeral(path + "/" + detailInfo);
            zoo.subscribeChildChanges(ROOT + "/" + service + PROVIDERS, listener);
        } else {
            // exporter
            path = ROOT + "/" + service + PROVIDERS;
            zoo.createPersistent(path);
            zoo.createEphemeral(path + "/" + detailInfo);

            if (SERVICE_MAP.containsKey(beaconPath.getService())) {
                SERVICE_MAP.get(beaconPath.getService()).add(detailInfo);
            } else {
                Set<String> sets = new HashSet<String>();
                sets.add(detailInfo);
                SERVICE_MAP.put(beaconPath.getService(), sets);
            }
            zoo.subscribeChildChanges(ROOT + "/" + service + CONSUMERS, listener);
        }
        LOG.warn("register service to zookeeper->{}", (path + "/" + detailInfo));
        CHILD_LISTENER_MAP.putIfAbsent(path, listener);
    }

    // 解析出必要的信息
    private void doResolveInfo(String parentPath, List<String> currentChilds) {
        String service = parentPath.split("/")[2];
        Set<String> sets = SERVICE_MAP.get(service);
        int childSize = currentChilds.size();
        List<String> clientList = new ArrayList<>();
        List<String> serverList = new ArrayList<>();
        for (String str : sets) {
            if (str.endsWith(From.CLIENT.name())) {
                clientList.add(str);
            } else {
                serverList.add(str);
            }
        }
        int clientSize = clientList.size();
        int serverSize = serverList.size();
        // server端监测到client有变化
        if (parentPath.endsWith(CONSUMERS)) {
            // 有client上线
            if (childSize > clientSize) {
                for (String str : currentChilds) {
                    if (!clientList.contains(str)) {
                        LOG.info("new client online->{}", str);
                        SERVICE_MAP.get(service).add(str);
                        break;
                    }
                }
            }
            // 有client下线
            else if (childSize < clientSize) {
                for (String str : clientList) {
                    if (!currentChilds.contains(str)) {
                        LOG.info("one client offline->{}", str);
                        SERVICE_MAP.get(service).remove(str);
                        break;
                    }
                }
            }

        }
        // client监听到server端有变化
        else {
            // 有server上线
            if (childSize > serverSize) {
                for (String str : currentChilds) {
                    if (!serverList.contains(str)) {
                        LOG.info("new server online->{}", str);
                        SERVICE_MAP.get(service).add(str);
                        break;
                    }
                }
            }
            // 有server下线
            else if (childSize < serverSize) {
                for (String str : serverList) {
                    if (!currentChilds.contains(str)) {
                        LOG.info("one server offline->{}", str);
                        SERVICE_MAP.get(service).remove(str);
                        break;
                    }
                }
            }

        }

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
        // SERVICE_MAP.remove(service);
        LOG.info("service->{} is shutdown.", service);
    }

    @Override
    public boolean doDiscoverService(String service) {
        String path = ROOT + "/" + service + PROVIDERS;
        return zoo.childrenNum(path) > 0 ? true : false;
    }

    @Override
    public void unregisterService(BeaconPath beaconPath) {
        // 这里是主动取消注册.比如有个server挂了,那么从本地缓存中去除这个server
        String path = null;
        String detailInfo = beaconPath.toPath();
        if (beaconPath.getSide() == From.CLIENT) {
            path = ROOT + "/" + beaconPath.getService() + CONSUMERS;
        } else if (beaconPath.getSide() == From.SERVER) {
            path = ROOT + "/" + beaconPath.getService() + PROVIDERS;
            Set<String> sets = SERVICE_MAP.get(beaconPath.getService());
            sets.remove(detailInfo);
        }

        zoo.remove(path + "/" + detailInfo);
        LOG.warn("unregister service in zookeeper->{}", (path + "/" + detailInfo));
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

    @Override
    public void close() {
        ZooUtil tzoo = zoo;
        tzoo.close();
    }
}
