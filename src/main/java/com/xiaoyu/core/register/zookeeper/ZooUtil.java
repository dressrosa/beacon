package com.xiaoyu.core.register.zookeeper;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
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

    private static final Logger LOG = LoggerFactory.getLogger("ZooUtil");

    private static final ZkSerializer SERIALIZER = new JsonSerializer();

    private static final Integer RETRY_TIMES = 10;

    /**
     * 临时节点消失的时间
     */
    private static final Integer SESSION_TIMEOUT = 30_000;
    private static final Integer CONNECTION_TIMEOUT = 3000;
    protected static final String HOST = "127.0.0.1";
    private ZkClient client;

    private ZooUtil() {
        client = new ZkClient(HOST, CONNECTION_TIMEOUT, SESSION_TIMEOUT, SERIALIZER);
        LOG.info("connected zookeeper:{}", HOST);
    }

    public static ZooUtil zoo() {
        ZooUtil zoo = null;
        int num = 0;
        try {
            zoo = new ZooUtil();
        } catch (Exception e) {
            e.printStackTrace();
            while (++num < RETRY_TIMES) {
                try {
                    zoo = new ZooUtil();
                    break;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
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

    public void createEphemeral(String path) {
        if (!exists(path)) {
            client.createEphemeral(path);
        }
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

    public void writeData(String path, Object data) {
        createPersistent(path);
        client.writeData(path, data);
    }

    /**
     * @param path
     * @param listener
     * @return
     */
    public void subscribeChildChanges(final String path, IZkChildListener listener) {
        // 检测是否已经创建
        createEphemeral(path);
        client.subscribeChildChanges(path, listener);
    }

    public Object readData(String path) {
        return client.readData(path);
    }

}
