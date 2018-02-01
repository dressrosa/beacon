package com.xiaoyu.transport;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * 客户端的channel
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconClientChannel extends AbstractBeaconChannel {

    private static final Logger LOG = LoggerFactory.getLogger("BeaconClientChannel");

    public BeaconClientChannel(Channel ch) {
        super(ch);
    }

    public static BeaconClientChannel getChannel(Channel ch) {
        BaseChannel beaconCh = CHANNEL_MAP.get(ch);
        if (beaconCh == null) {
            CHANNEL_MAP.putIfAbsent(ch, (beaconCh = new BeaconClientChannel(ch)));
        }
        LOG.info("map Size:{}", CHANNEL_MAP.size());
        return (BeaconClientChannel) beaconCh;
    }

    /**
     * 随机获取一个channel
     * 
     * @return
     * @throws Exception
     */
    public static BeaconClientChannel getChannel() throws Exception {
        if (!CHANNEL_MAP.isEmpty()) {
            Iterator<BaseChannel> iter = CHANNEL_MAP.values().iterator();
            while (iter.hasNext()) {
                BaseChannel ch = iter.next();
                if (ch instanceof BeaconClientChannel) {
                    return (BeaconClientChannel) ch;
                }
            }
            LOG.info("map Size:{}", CHANNEL_MAP.size());
        }
        throw new Exception("CHANNEL_MAP is null");
    }

    @Override
    public Future<Object> send(Object message) throws Exception {
        try {
            super.send(message);
            Future<Object> taskFuture = TASK_POOL.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    // 对同一次的请求channel加锁,当收到结果时释放
                    synchronized (channel) {
                        ChannelFuture future = channel.pipeline().context("beaconClientHandler").writeAndFlush(message);
                        future.get();
                        channel.wait();
                    }
                    // 已获取到结果
                    return getResult();
                }
            });
            return taskFuture;
        } finally {

        }
    }

    @Override
    public void receive(Object message) throws Exception {
        // 对同一此的请求channel加锁,当收到结果时释放
        synchronized (channel) {
            super.receive(message);
            // 触发下一个handler的读操作
            this.channel.pipeline().context("beaconClientHandler").read();
            // 通知阻塞的发送操作
            channel.notifyAll();
        }
    }

    public void removeChannel(Channel channel) {
        CHANNEL_MAP.remove(channel);
    }

}
