/**
 * 
 */
package com.xiaoyu.core.register.zookeeper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.apache.zookeeper.Watcher.Event.KeeperState;
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

    // consumer or provider path -> listener
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
                LOG.info("Service changed:parentPath->{},currentChilds->{}", parentPath, currentChilds);
                if (currentChilds == null) {
                    return;
                }
                doResolveInfo(parentPath, currentChilds);
            }
        };
        // 初始化service父节点
        if (beaconPath.getSide().equals(From.CLIENT)) {
            // reference
            if (!this.discoverService(beaconPath.getService())) {
                LOG.error("Cannot find the service->{} in zookeeper,please check.", beaconPath.getService());
                return;
            }
            path = this.consumerPath(service);
            zoo.createPersistent(path);
            zoo.createEphemeral(path + "/" + detailInfo);
            // 监听操作在执行的时候,如果path不存在,会生成临时节点,但是临时节点是不能创建子节点的,这里首先判断下
            String providerPath = this.providerPath(service);
            zoo.createPersistent(providerPath);
            zoo.subscribeChildChanges(providerPath, listener);
            LOG.info("Subscribe service in zookeeper->{}", providerPath);

            // 初始化provider本地缓存
            initProviders(service);
        } else {
            // exporter
            path = this.providerPath(service);
            zoo.createPersistent(path);
            zoo.createEphemeral(path + "/" + detailInfo);

            if (SERVICE_MAP.containsKey(beaconPath.getService())) {
                SERVICE_MAP.get(beaconPath.getService()).add(beaconPath);
            } else {
                Set<BeaconPath> sets = new HashSet<>();
                sets.add(beaconPath);
                SERVICE_MAP.put(beaconPath.getService(), sets);
            }
            // 同上
            String consumerPath = this.consumerPath(service);
            zoo.createPersistent(consumerPath);
            zoo.subscribeChildChanges(consumerPath, listener);
            LOG.info("Subscribe service in zookeeper->{}", consumerPath);
        }
        LOG.info("Register service to zookeeper->{}", (path + "/" + detailInfo));
        CHILD_LISTENER_MAP.putIfAbsent(path, listener);
    }

    /**
     * 修复因为异常原因(不知道为啥)导致的server端在线,但是zoo的provider节点child丢失
     */
    public void doRecoverProviders() {
        // TODO
    }

    /**
     * 解析出必要的信息
     * 
     * @param parentPath
     * @param currentChilds
     */
    private void doResolveInfo(String parentPath, List<String> currentChilds) {
        // ---/beacon/service/xxxx
        String service = parentPath.split("/")[2];
        Set<BeaconPath> sets = SERVICE_MAP.get(service);
        int childSize = currentChilds.size();
        List<String> consumerList = new ArrayList<>();
        List<String> providerList = new ArrayList<>();
        // 将client和server分开
        for (BeaconPath detail : sets) {
            if (detail.getSide() == From.CLIENT) {
                consumerList.add(detail.toPath());
            } else {
                providerList.add(detail.toPath());
            }
        }
        int consumerSize = consumerList.size();
        int providerSize = providerList.size();
        // server端监测到client有变化
        if (parentPath.endsWith(CONSUMERS)) {
            // 有client上线
            if (childSize > consumerSize) {
                for (String detail : currentChilds) {
                    if (!consumerList.contains(detail)) {
                        LOG.info("New client online->{}", detail);
                        SERVICE_MAP.get(service).add(BeaconPath.toEntity(detail));
                        break;
                    }
                }
            }
            // 有client下线
            else if (childSize < consumerSize) {
                for (String detail : consumerList) {
                    if (!currentChilds.contains(detail)) {
                        LOG.info("One client offline->{}", detail);
                        SERVICE_MAP.get(service).remove(BeaconPath.toEntity(detail));
                        break;
                    }
                }
            }
        }
        // client监听到server端有变化
        else {
            // 有server上线
            if (childSize > providerSize) {
                for (String detail : currentChilds) {
                    if (!providerList.contains(detail)) {
                        LOG.info("New server online->{}", detail);
                        SERVICE_MAP.get(service).add(BeaconPath.toEntity(detail));
                        break;
                    }
                }
            }
            // 有server下线
            else if (childSize < providerSize) {
                for (String detail : providerList) {
                    if (!currentChilds.contains(detail)) {
                        LOG.info("One server offline->{}", detail);
                        SERVICE_MAP.get(service).remove(BeaconPath.toEntity(detail));
                        // TODO 是否关闭对应的client
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void unregisterService(BeaconPath beaconPath) {
        // 这里是主动取消注册.比如有个server挂了,那么从本地缓存中去除这个server
        String path = null;
        String detailInfo = beaconPath.toPath();
        if (beaconPath.getSide() == From.CLIENT) {
            path = this.consumerPath(beaconPath.getService());
            // client取消对server端的监听
            String providerPath = this.providerPath(beaconPath.getService());
            zoo.subscribeChildChanges(providerPath,
                    CHILD_LISTENER_MAP.get(providerPath));
            CHILD_LISTENER_MAP.remove(providerPath);
            LOG.info("Unsubscribe service in zookeeper->{}", providerPath);
        } else if (beaconPath.getSide() == From.SERVER) {
            path = this.providerPath(beaconPath.getService());
            Set<BeaconPath> sets = SERVICE_MAP.get(beaconPath.getService());
            sets.remove(beaconPath);
            // server端取消对client的监听
            String consumerPath = this.consumerPath(beaconPath.getService());
            zoo.subscribeChildChanges(consumerPath,
                    CHILD_LISTENER_MAP.get(consumerPath));
            CHILD_LISTENER_MAP.remove(consumerPath);
            LOG.info("Unsubscribe service in zookeeper->{}", consumerPath);
        }
        zoo.remove(path + "/" + detailInfo);
        LOG.info("Unregister service in zookeeper->{}", (path + "/" + detailInfo));

    }

    @Override
    public void address(String addr) {
        try {
            zoo = ZooUtil.zoo(addr);
            IZkStateListener listener = new IZkStateListener() {
                @Override
                public void handleStateChanged(KeeperState state) throws Exception {
                    LOG.info("Get zookeeper state->" + state.name());
                }

                @Override
                public void handleSessionEstablishmentError(Throwable error) throws Exception {
                    error.printStackTrace();
                }

                @Override
                public void handleNewSession() throws Exception {
                    // 由于网络等原因使与zookeeper失联最后导致session out,这里进行数据恢复
                    doRecover();
                }
            };
            zoo.subscribeStateChanges(listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        zoo.createPersistent(ROOT);
    }

    private void doRecover() {
        LOG.info("Revover zookeeper session data.");
        // 恢复service
        Iterator<Entry<String, Set<BeaconPath>>> iter = SERVICE_MAP.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Set<BeaconPath>> entry = iter.next();
            Set<BeaconPath> set = entry.getValue();
            String service = entry.getKey();
            for (BeaconPath detail : set) {
                // 这里client 和 server都会调用,同时建立相同的path不会冲突
                if (detail.getSide() == From.SERVER) {
                    zoo.createEphemeral(this.providerPath(service) + "/" + detail.toPath());
                } else {
                    zoo.createEphemeral(this.providerPath(service) + "/" + detail.toPath());
                }
            }
        }
        // 恢复listener
        Iterator<Entry<String, IZkChildListener>> citer = CHILD_LISTENER_MAP.entrySet().iterator();
        while (citer.hasNext()) {
            Entry<String, IZkChildListener> entry = citer.next();
            IZkChildListener listener = entry.getValue();
            String sidePath = entry.getKey();
            // 先取消,无法判断是否之前的还在
            zoo.unsubscribeChildChanges(sidePath, listener);
            zoo.subscribeChildChanges(sidePath, listener);
        }
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

    private final String providerPath(String service) {
        StringBuilder builder = new StringBuilder();
        builder.append(ROOT).append("/").append(service).append(PROVIDERS);
        return builder.toString();
    }

    private final String consumerPath(String service) {
        StringBuilder builder = new StringBuilder();
        builder.append(ROOT).append("/").append(service).append(CONSUMERS);
        return builder.toString();
    }

    @Override
    public boolean doDiscoverService(String service) {
        // 查找是否有provider
        String path = this.providerPath(service);
        return zoo.childrenNum(path) > 0 ? true : false;
    }

    /**
     * client启动时,本地并没有对应的已存在的provider,这里初始化所属的provider
     */
    @Override
    public void doInitProviders(String service) {
        ZooUtil tzoo = zoo;
        List<String> childList = tzoo.children(this.providerPath(service));
        for (String detail : childList) {
            SERVICE_MAP.get(service).add(BeaconPath.toEntity(detail));
        }
    }
}
