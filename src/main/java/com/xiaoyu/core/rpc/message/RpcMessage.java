package com.xiaoyu.core.rpc.message;

/**
 * @author hongyu
 * @date 2018-02-01
 * @description
 */
public class RpcMessage {

    private String id;

    /**
     * 0 client 1 server
     */
    private byte from;

    /**
     * 是否心跳消息
     */
    private boolean isHeartbeat;

    public byte getFrom() {
        return from;
    }

    public RpcMessage setFrom(byte from) {
        this.from = from;
        return this;
    }

    public String getId() {
        return id;
    }

    public RpcMessage setId(String id) {
        this.id = id;
        return this;
    }

    public boolean isHeartbeat() {
        return isHeartbeat;
    }

    public RpcMessage setHeartbeat(boolean isHeartbeat) {
        this.isHeartbeat = isHeartbeat;
        return this;
    }

}
