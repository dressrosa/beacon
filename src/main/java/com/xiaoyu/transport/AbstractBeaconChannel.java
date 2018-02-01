package com.xiaoyu.transport;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.Channel;

/**
 * 封装channel 提供基础的核心变量
 * 用于消息的收发
 * @author hongyu
 * @date 2018-02
 * @description
 */
public abstract class AbstractBeaconChannel implements BaseChannel {

    /**
     * 由下层提供,为netty的发送和接收
     */
    protected Channel channel;

    /**
     * 从server断获取的结果,用于异步获取
     */
    private Object result;

    /**
     * 提供给下层获取请求结果
     * 
     * @return
     */
    public Object getResult() {
        return this.result;
    }

    private static final AtomicInteger COUNT = new AtomicInteger(0);

    /**
     * 线程池,每一个消费请求都会放入池中执行等待结果
     */
    protected static final ExecutorService TASK_POOL = new ThreadPoolExecutor(10, 10,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "BeaconTaskHandler-" + COUNT.getAndIncrement());
                }
            });

    /**
     * 每一个channel都会生成对应的BaseChannel,用于对channel的存储
     */
    protected static final ConcurrentMap<Channel, BaseChannel> CHANNEL_MAP = new ConcurrentHashMap<>(16);

    public AbstractBeaconChannel(Channel ch) {
        this.channel = ch;
    }

    @Override
    public Future<Object> send(Object message) throws Exception {
        if (message == null) {
            throw new Exception("message be sent is null.");
        }
        return null;
    }

    @Override
    public void receive(Object message) throws Exception {
        if (message == null) {
            throw new Exception("message received is null.");
        }
        this.result = message;
        return;
    }
}
