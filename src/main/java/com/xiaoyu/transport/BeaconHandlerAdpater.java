package com.xiaoyu.transport;

import java.util.concurrent.Future;

import com.xiaoyu.transport.api.BeaconHandler;
import com.xiaoyu.transport.support.AbstractBeaconChannel;

import io.netty.channel.Channel;

/**
 * 用于消息的处理
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconHandlerAdpater implements BeaconHandler {

    /**
     * 持有client和server在启动时产生的channel引用
     */
    protected Channel channel;

    private BeaconHandler handler;

    public BeaconHandlerAdpater(BeaconHandler handler) {
        this.handler = handler;
    }

    @Override
    public void receive(Object message) throws Exception {
        if (message == null) {
            throw new Exception("接受消息为空");
        }
        if (handler instanceof BeaconServerHandler) {
            ((BeaconServerHandler) handler).receive(message);
        } else {
            ((BeaconClientHandler) handler).receive(message);
        }
    }

    @Override
    public Future<Object> send(Object message) throws Exception {
        if (message == null) {
            throw new Exception("发送消息为空");
        }
        if (handler instanceof BeaconServerHandler) {
            return ((BeaconServerHandler) handler).send(message);
        } else {
            return ((BeaconClientHandler) handler).send(message);
        }
    }

    public void stop() {
        // 正常的client或server关闭后,线程池并没有关闭
        // 这里主动通知线程池关闭
        AbstractBeaconChannel.TASK_POOL.shutdown();
    }

}
