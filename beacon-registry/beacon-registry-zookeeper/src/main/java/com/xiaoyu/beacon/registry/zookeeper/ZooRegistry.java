/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.registry.zookeeper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.beacon.common.bean.BeaconPath;
import com.xiaoyu.beacon.common.constant.From;
import com.xiaoyu.beacon.common.exception.BeaconException;
import com.xiaoyu.beacon.common.utils.NetUtil;
import com.xiaoyu.beacon.registry.AbstractRegistry;

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

    /**
     * consumer or provider path -> listener
     */
    private static final ConcurrentMap<String, IZkChildListener> CHILD_LISTENER_MAP = new ConcurrentHashMap<>(32);

    /**
     * 用来监听provider异常丢失
     */
    private static ScheduledExecutorService providerMonitor;

    /**
     * 格式: /beacon/service-name/consumers/service-detail-info
     */
    @Override
    public void registerService(BeaconPath beaconPath) {
        // 找到需要暴漏的service,然后写入providers信息,或者客户端启动写入consumers信息
        String service = beaconPath.getService();
        zoo.createPersistent(ROOT + "/" + service);
        String path = null;
        // server端监听client,client监听server端的
        IZkChildListener listener = new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Service changed:parentPath->{},currentChilds->{}", parentPath, currentChilds);
                }
                if (currentChilds == null) {
                    return;
                }
                doResolveInfo(parentPath, currentChilds);
            }
        };
        String detailInfo = beaconPath.toPath();
        // 初始化service父节点
        if (beaconPath.getSide() == From.CLIENT) {
            // reference(client)
            // 启动时检查
            if (beaconPath.isCheck() && !super.discoverService(service)) {
                LOG.error("Cannot find providers of the service->{} in zookeeper,please check.", service);
                try {
                    throw new BeaconException(
                            "Cannot find providers of the service->" + service + " in zookeeper,please check.");
                } catch (Exception e) {
                    LOG.error("" + e);
                    return;
                }
            }
            path = this.consumerPath(service);
            zoo.createPersistent(path);
            LOG.info("Register consumer service to zookeeper->{}", (path + "/" + detailInfo));
            zoo.createEphemeral(this.fullPath(path, detailInfo));
            // 监听操作在执行的时候,如果path不存在,会生成临时节点,但是临时节点是不能创建子节点的,这里首先判断下
            String providerPath = this.providerPath(service);
            zoo.createPersistent(providerPath);
            zoo.subscribeChildChanges(providerPath, listener);
            LOG.info("Subscribe provider service in zookeeper->{}", providerPath);
            // 初始化provider本地缓存
            initProviders(service);
        } else {
            // exporter(server)
            path = this.providerPath(service);
            zoo.createPersistent(path);
            LOG.info("Register provider service to zookeeper->{}", (path + "/" + detailInfo));
            zoo.createEphemeral(this.fullPath(path, detailInfo));
            // 进行监听某一个具体service,这里会多次调用 ,但是只监听第一个就够了
            this.monitorProvider(service, detailInfo);

            // 同reference
            String consumerPath = this.consumerPath(service);
            zoo.createPersistent(consumerPath);
            zoo.subscribeChildChanges(consumerPath, listener);
            LOG.info("Subscribe consumer service in zookeeper->{}", consumerPath);
        }
        // 保存本地
        this.storeLocalService(service, beaconPath);
        // 保存listener
        CHILD_LISTENER_MAP.putIfAbsent(path, listener);
    }

    /**
     * TODO
     * 因为异常原因(不知道为啥)导致的server端在线
     * ,但是zoo的provider节点child丢失
     * 这里单独进行监听其中一个就行
     */
    private void monitorProvider(String service, String detailInfo) {
        if (providerMonitor != null) {
            return;
        }
        LOG.debug("-Monitor current provider whether lost or not-");
        providerMonitor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "provider-monitor-1");
                t.setDaemon(true);
                return t;
            }
        });
        providerMonitor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                String providerPath = providerPath(service);
                List<String> list = zoo.children(providerPath);
                // provider丢失
                if (!list.contains(detailInfo)) {
                    // 进行provider数据恢复
                    doRecoverProviders();
                }
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    /**
     * 修复因为异常原因导致的server端在线
     * ,但是zoo的provider节点child丢失
     */
    private void doRecoverProviders() {
        LOG.info("Recover zookeeper session data when something error "
                + "happened in connection between zoo and provider");
        // 恢复service
        Iterator<Entry<String, Set<BeaconPath>>> iter = Provider_Service_Map.entrySet().iterator();
        String host = NetUtil.localIP();
        Entry<String, Set<BeaconPath>> entry = null;
        Set<BeaconPath> set = null;
        while (iter.hasNext()) {
            entry = iter.next();
            set = entry.getValue();
            for (BeaconPath p : set) {
                // 当前服务器提供的provider
                if (host.equals(p.getHost())) {
                    zoo.createEphemeral(this.fullPath(this.providerPath(entry.getKey()), p.toPath()));
                }
            }
        }
        Iterator<Entry<String, Set<BeaconPath>>> citer = Consumer_Service_Map.entrySet().iterator();
        Entry<String, Set<BeaconPath>> centry = null;
        Set<BeaconPath> cset = null;
        while (citer.hasNext()) {
            centry = citer.next();
            cset = centry.getValue();
            for (BeaconPath p : cset) {
                // 发现consumer也可能丢
                zoo.createEphemeral(this.fullPath(this.consumerPath(centry.getKey()), p.toPath()));
            }
        }
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
        Set<BeaconPath> psets = Provider_Service_Map.get(service);
        Set<BeaconPath> csets = Consumer_Service_Map.get(service);
        int childSize = currentChilds.size();
        List<String> consumerList = new ArrayList<>();
        List<String> providerList = new ArrayList<>();
        // 本地缓存
        if (psets != null) {
            for (BeaconPath path : psets) {
                providerList.add(path.toPath());
            }
        }
        if (csets != null) {
            for (BeaconPath path : csets) {
                consumerList.add(path.toPath());
            }
        }
        int consumerSize = consumerList.size();
        int providerSize = providerList.size();
        // server端监测到client有变化
        if (parentPath.endsWith(CONSUMERS)) {
            // 有client上线
            if (childSize > consumerSize) {
                for (String s : currentChilds) {
                    if (!consumerList.contains(s)) {
                        LOG.info("store consumer service ->{}", s);
                        this.storeLocalService(service, BeaconPath.toEntity(s));
                        break;
                    }
                }
            }
            // 有client下线
            else if (childSize < consumerSize) {
                for (String s : consumerList) {
                    if (!currentChilds.contains(s)) {
                        LOG.info("remove consumer service->{}", s);
                        Consumer_Service_Map.get(service).remove(BeaconPath.toEntity(s));
                        break;
                    }
                }
            }
        }
        // client监听到server端有变化
        else {
            // 有server上线
            if (childSize > providerSize) {
                for (String s : currentChilds) {
                    if (!providerList.contains(s)) {
                        LOG.info("store provider service ->{}", s);
                        this.storeLocalService(service, BeaconPath.toEntity(s));
                        break;
                    }
                }
            }
            // 有server下线
            else if (childSize < providerSize) {
                // TODO 这里可能是server关闭后,又启动了,但是session消息的时间是根据SESSION_TIMEOUT设定的
                // 所以SESSION_TIMEOUT后session才会消失,也就是说server
                // SESSION_TIMEOUT内再次启动后,注册的节点都会因为session失效而消失.
                // 因此导致client没收到server启动通知反而收到server关闭的通知.
                // 这里需要考虑session timeout和server重启时差的平衡性
                for (String s : providerList) {
                    if (!currentChilds.contains(s)) {
                        LOG.info("remove provider service->{}", s);
                        removeLocalService(service, BeaconPath.toEntity(s));
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
        String sidePath = null;
        String detailInfo = beaconPath.toPath();
        if (beaconPath.getSide() == From.CLIENT) {
            path = this.consumerPath(beaconPath.getService());
            // client取消对server端的监听
            sidePath = this.providerPath(beaconPath.getService());
        } else if (beaconPath.getSide() == From.SERVER) {
            path = this.providerPath(beaconPath.getService());
            super.removeLocalService(beaconPath.getService(), beaconPath);
            // server端取消对client的监听
            sidePath = this.consumerPath(beaconPath.getService());
        }
        final ConcurrentMap<String, IZkChildListener> lmap = CHILD_LISTENER_MAP;
        LOG.info("Unsubscribe service in zookeeper->{}", sidePath);
        zoo.unsubscribeChildChanges(sidePath, lmap.get(sidePath));
        lmap.remove(sidePath);
        LOG.info("Unregister service in zookeeper->{}", this.fullPath(path, detailInfo));
        zoo.remove(this.fullPath(path, detailInfo));
    }

    @Override
    public void address(String addr) {
        try {
            zoo = ZooUtil.zoo(addr);
            IZkStateListener listener = new IZkStateListener() {
                @Override
                public void handleStateChanged(KeeperState state) throws Exception {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Get zookeeper state->" + state.name());
                    }
                }

                @Override
                public void handleSessionEstablishmentError(Throwable error) throws Exception {
                    if (LOG.isDebugEnabled()) {
                        LOG.error("HandleSessionEstablishmentError->", error);
                    }
                }

                @Override
                public void handleNewSession() throws Exception {
                    doRecover();
                }
            };
            zoo.subscribeStateChanges(listener);
        } catch (Exception e) {
            LOG.error("" + e);
        }
        zoo.createPersistent(ROOT);
    }

    /**
     * 由于网络等原因使与zookeeper失联
     * 最后导致session out,这里进行数据恢复
     */
    private void doRecover() {
        LOG.info("Revover zookeeper session data.");
        // 恢复service
        Iterator<Entry<String, Set<BeaconPath>>> iter = Provider_Service_Map.entrySet().iterator();
        Entry<String, Set<BeaconPath>> entry = null;
        Set<BeaconPath> set = null;
        while (iter.hasNext()) {
            entry = iter.next();
            set = entry.getValue();
            for (BeaconPath p : set) {
                // 这里client 和 server都会调用,同时建立相同的path不会冲突
                zoo.createEphemeral(this.fullPath(this.providerPath(entry.getKey()), p.toPath()));
            }
        }
        Iterator<Entry<String, Set<BeaconPath>>> citer = Consumer_Service_Map.entrySet().iterator();
        Entry<String, Set<BeaconPath>> centry = null;
        Set<BeaconPath> cset = null;
        while (citer.hasNext()) {
            centry = citer.next();
            cset = centry.getValue();
            for (BeaconPath p : cset) {
                zoo.createEphemeral(this.fullPath(this.consumerPath(centry.getKey()), p.toPath()));
            }
        }
        // 恢复listener
        Iterator<Entry<String, IZkChildListener>> liter = CHILD_LISTENER_MAP.entrySet().iterator();
        Entry<String, IZkChildListener> en = null;
        IZkChildListener listener = null;
        while (citer.hasNext()) {
            en = liter.next();
            listener = en.getValue();
            String sidePath = en.getKey();
            // 先取消,无法明确判断是否之前的还在
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
        // 还没执行到监视器可能就结束了
        if (providerMonitor != null) {
            // 关闭检查器
            providerMonitor.shutdown();
        }
        ZooUtil tzoo = zoo;
        final ConcurrentMap<String, Set<BeaconPath>> cmap = Consumer_Service_Map;
        // TODO 关闭可能未处理的beaconPath,比如泛型的
        if (!cmap.isEmpty()) {
            Iterator<Set<BeaconPath>> setsIter = cmap.values().iterator();
            Set<BeaconPath> allSet = new HashSet<>();
            while (setsIter.hasNext()) {
                allSet.addAll(setsIter.next());
            }
            Iterator<BeaconPath> pathIter = allSet.iterator();
            while (pathIter.hasNext()) {
                this.unregisterService(pathIter.next());
            }
        }
        // 关闭所有
        tzoo.unsubscribeAll();
        tzoo.close();
    }

    private final String providerPath(String service) {
        StringBuilder builder = new StringBuilder();
        builder.append(ROOT).append("/").append(service).append(PROVIDERS);
        return builder.toString();
    }

    private final String fullPath(String path, String detailInfo) {
        StringBuilder builder = new StringBuilder();
        builder.append(path).append("/").append(detailInfo);
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
            super.storeLocalService(service, BeaconPath.toEntity(detail));
        }
    }

}
