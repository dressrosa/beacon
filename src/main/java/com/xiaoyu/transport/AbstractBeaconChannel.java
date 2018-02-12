package com.xiaoyu.transport;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.xiaoyu.core.rpc.message.CallbackListener;

import io.netty.channel.Channel;

/**
 * 封装channel 提供基础的核心变量 用于消息的收发
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public abstract class AbstractBeaconChannel implements BaseChannel {

    /**
     * 由下层提供,为netty的发送和接收
     */
    protected Channel channel;

    protected BeaconHandler beaconHandler;

    /**
     * 从server断获取的结果,用于异步获取 requestId->result
     */
    private static final ConcurrentMap<String, CallbackListener> RESULT_MAP = new ConcurrentHashMap<>(8);

    /**
     * 每一个channel都会生成对应的BaseChannel,用于对channel的存储
     */
    private static final ConcurrentMap<Channel, BaseChannel> CHANNEL_MAP = new ConcurrentHashMap<>(16);

    /**
     * 请求端 server or client
     */
    private String side;

    /**
     * 用于线程池中线程的计数
     */
    private static final AtomicInteger COUNT = new AtomicInteger(0);

    /**
     * 线程池,每一个消费请求都会放入池中执行等待结果
     */
    protected static final ThreadPoolExecutor TASK_POOL = new ThreadPoolExecutor(10, 10,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "BeaconTaskHandler-" + COUNT.getAndIncrement());
                }
            });

    public AbstractBeaconChannel(Channel ch, BeaconHandler beaconHandler) {
        this.channel = ch;
        this.beaconHandler = beaconHandler;
    }

    public String getSide() {
        return side;
    }

    public AbstractBeaconChannel setSide(String side) {
        this.side = side;
        return this;
    }

    /**
     * 提供给下层
     * 
     * @return
     */
    public void addListener(String requestId, CallbackListener listener) {
        RESULT_MAP.put(requestId, listener);
    }

    /**
     * 提供给下层
     * 
     * @return
     */
    public void setResult(String requestId, Object result) {
        CallbackListener listener;
        synchronized ((listener = RESULT_MAP.get(requestId))) {
            listener.onSuccess(result);
            // 通知等待线程,这里只有一个线程在等待
            listener.notify();
            RESULT_MAP.remove(requestId);
        }
    }

    public static BaseChannel getChannel(Channel ch, BeaconHandler beaconHandler) throws Exception {
        BaseChannel beaconCh = CHANNEL_MAP.get(ch);
        if (beaconHandler instanceof BeaconClientHandler) {
            CHANNEL_MAP.putIfAbsent(ch, (beaconCh = new BeaconClientChannel(ch, beaconHandler).setSide("client")));
        } else {
            CHANNEL_MAP.putIfAbsent(ch, (beaconCh = new BeaconServerChannel(ch, beaconHandler).setSide("server")));
        }

        return beaconCh;
    }

    public void removeChannel(Channel channel) {
        CHANNEL_MAP.remove(channel);
    }

    @Override
    public Future<Object> send(Object message) throws Exception {
        if (message == null) {
            throw new Exception("message be sent is null.");
        }
        return this.doSend(message);
    }

    @Override
    public void receive(Object message) throws Exception {
        if (message == null) {
            throw new Exception("message received is null.");
        }
        this.doReceive(message);
        return;
    }

    /**
     * 由具体的client or server的channel处理具体的发送操作
     * 
     * @param message
     * @return
     */
    protected abstract Future<Object> doSend(Object message);

    /**
     * 由具体的client or server的channel处理具体的接受操作
     * 
     * @param message
     */
    protected abstract void doReceive(Object message);
}
