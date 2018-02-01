package com.xiaoyu.core.register.zookeeper;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class ZooUtil {

    private static final Logger LOG = LoggerFactory.getLogger("ZooUtil");

    /**
     * 临时节点消失的时间
     */
    private static final Integer SESSION_TIMEOUT = 5000;
    private static final Integer CONNECTION_TIMEOUT = 5000;
    private static final String HOST = "localhost";
    private ZkClient client;

    public ZooUtil() {
        client = new ZkClient(HOST, CONNECTION_TIMEOUT, SESSION_TIMEOUT, new JsonSerializer());
    }

    public void createPersistent(String path) {
        if (!client.exists(path)) {
            client.createPersistent(path);
        }
    }

    public void createEphemeral(String path) {
        if (!client.exists(path)) {
            client.createEphemeral(path);
        }
    }

    private boolean isOver = false;

    public boolean isOver() {
        return isOver;
    }

    public void setOvered() {
        this.isOver = true;
        client.close();
    }

    public void subscribeDataChanges(final String path) {
        // 检测是否已经创建
        createPersistent(path);

        client.subscribeDataChanges(path, new IZkDataListener() {
            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
            }

            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
            }
        });

    }

    public void writeData(String path, Object data) {
        createPersistent(path);
        Stat s = new Stat();
        client.readData(path, s);
        client.writeDataReturnStat(path, "", 1);
    }

    public void subscribeChildChanges(final String path) {
        // 检测是否已经创建
        createEphemeral(path);

        client.subscribeChildChanges(path, new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                System.out.println(parentPath + "的孩子变了:" + currentChilds);
            }
        });

    }

    public Object readData(String path) {
        return client.readData(path);
    }
}
