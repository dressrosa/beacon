/**
 * 唯有读书,不慵不扰
 * 
 */
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
     * 临时节点消失的时间
     */
    private static final Integer SESSION_TIMEOUT = 2_000;
    /**
     * 连接超时 3s
     */
    private static final Integer CONNECTION_TIMEOUT = 3_000;
    private ZkClient client;

    private ZooUtil(String host) {
        client = new ZkClient(host, SESSION_TIMEOUT, CONNECTION_TIMEOUT, SERIALIZER);
        LOG.info("Connected zookeeper->{} success.", host);
    }

    public static ZooUtil zoo(String host) throws Exception {
        ZooUtil zoo = null;
        int num = 0;
        try {
            zoo = new ZooUtil(host);
        } catch (Exception e) {
            LOG.error("Connect to zookeeper->{} failed,go into retry.", host, e);
            try {
                while (++num < RETRY_TIMES) {
                    zoo = new ZooUtil(host);
                    TimeUnit.MILLISECONDS.sleep(1000);
                    if (zoo != null) {
                        break;
                    }
                }
            } catch (Exception e1) {
                LOG.error("Connect to zookeeper error->", e1);
            }
        }
        if (zoo == null) {
            throw new Exception("Connect to zookeep->" + host + " failed,please start zookeeper first");
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

    public void subscribeStateChanges(final IZkStateListener listener) {
        client.subscribeStateChanges(listener);
    }

    public void unsubscribeAll() {
        client.unsubscribeAll();
    }

    public void writeData(String path, Object data) {
        createPersistent(path);
        client.writeData(path, data);
    }

    public void unsubscribeDataChanges(final String path, final IZkDataListener listener) {
        client.unsubscribeDataChanges(path, listener);
    }

    public int childrenNum(final String path) {
        return client.countChildren(path);
    }

    public void close() {
        client.close();
    }

    public void subscribeChildChanges(final String path, final IZkChildListener listener) {
        client.subscribeChildChanges(path, listener);
    }

    public void unsubscribeChildChanges(final String path, final IZkChildListener listener) {
        client.unsubscribeChildChanges(path, listener);
    }

    public Object readData(String path) {
        return client.readData(path);
    }

}
