package com.xiaoyu.core.register.zookeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class ZooUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ZooUtil.class);

    private static final ZkSerializer SERIALIZER = new JsonSerializer();

    private static final Integer RETRY_TIMES = 10;

    /**
     * 临时节点消失的时间 30s
     */
    private static final Integer SESSION_TIMEOUT = 30_000;
    /**
     * 连接超时 3s
     */
    private static final Integer CONNECTION_TIMEOUT = 3_000;
    private ZkClient client;

    private ZooUtil(String host) {
        client = new ZkClient(host, SESSION_TIMEOUT, CONNECTION_TIMEOUT, SERIALIZER);
        LOG.info("connected zookeeper->{}", host);
    }

    public static ZooUtil zoo(String host) throws Exception {
        ZooUtil zoo = null;
        int num = 0;
        try {
            zoo = new ZooUtil(host);
        } catch (Exception e) {
            LOG.error("connect to zookeeper->{} failed,go into retry.", host, e);
            while (++num < RETRY_TIMES) {
                try {
                    zoo = new ZooUtil(host);
                    TimeUnit.MILLISECONDS.sleep(500);
                    if (zoo != null) {
                        break;
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        if (zoo == null) {
            throw new Exception("connect to zookeep->" + host + " failed");
        }
        return zoo;
    }

    public void createPersistent(String path) {
        if (!exists(path)) {
            client.createPersistent(path);
        }
    }

    public boolean exists(String path) {
        return client.exists(path);
    }

    public boolean remove(String path) {
        return client.delete(path);
    }

    public void createEphemeral(String path) {
        if (!exists(path)) {
            client.createEphemeral(path);
        }
    }

    public List<String> children(String path) {
        List<String> childList = client.getChildren(path);
        if (childList == null) {
            childList = new ArrayList<>(0);
        }
        return childList;
    }

    /**
     * 当server端增加或掉线的时候,获取通知,更新缓存的server地址
     * 
     * @param path
     * @return
     */
    public IZkDataListener subscribeDataChanges(final String path) {
        // 检测是否已经创建
        createPersistent(path);
        IZkDataListener listener = null;
        client.subscribeDataChanges(path, listener = new IZkDataListener() {
            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                // do nothing
            }

            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                // do nothing
            }
        });
        return listener;
    }

    public void subscribeStateChanges(IZkStateListener listener) {
        client.subscribeStateChanges(listener);
    }

    public void writeData(String path, Object data) {
        createPersistent(path);
        client.writeData(path, data);
    }

    public void unsubscribeDataChanges(final String path, IZkDataListener listener) {
        client.unsubscribeDataChanges(path, listener);
    }

    public int childrenNum(final String path) {
        return client.countChildren(path);
    }

    public void close() {
        client.close();
    }

    public void subscribeChildChanges(final String path, IZkChildListener listener) {
        client.subscribeChildChanges(path, listener);
    }

    public void unsubscribeChildChanges(final String path, IZkChildListener listener) {
        client.unsubscribeChildChanges(path, listener);
    }

    public Object readData(String path) {
        return client.readData(path);
    }

}
