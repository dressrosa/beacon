/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.transport.support;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.beacon.common.message.CallbackListener;
import com.xiaoyu.beacon.transport.api.BaseChannel;

/**
 * 封装channel 提供基础的核心变量 用于消息的收发
 * 
 * @author hongyu
 * @date 2018-02
 * @description 对channel的接收发送的结果线程池的管理
 */
public abstract class AbstractBeaconChannel implements BaseChannel {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractBeaconChannel.class);
    /**
     * 从server获取的结果,用于异步获取 requestId->result
     */
    private static final ConcurrentMap<String, CallbackListener> RESULT_MAP = new ConcurrentHashMap<>(32);

    /**
     * 请求端 server or client
     */
    private String side;

    /**
     * 线程池,每一个消费请求都会放入池中执行等待结果,相当于newCachedThreadPool
     * coresize=处理器+1
     */
    private static final ThreadPoolExecutor TASK_POOL = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() + 1, Runtime.getRuntime().availableProcessors() << 1,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), new ThreadFactory() {
                private final AtomicInteger COUNT = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "BeaconTaskHandler-" + COUNT.getAndIncrement());
                }
            });

    public String getSide() {
        return side;
    }

    public AbstractBeaconChannel setSide(String side) {
        this.side = side;
        return this;
    }

    public static void notifyCloseTaskPool() {
        final ThreadPoolExecutor pool = TASK_POOL;
        pool.shutdown();
        LOG.info("Shutdown the beacon channel task pool.");
    }

    public Future<Object> addTask(Callable<Object> call) {
        final ThreadPoolExecutor pool = TASK_POOL;
        return pool.submit(call);
    }

    public void addTask(Runnable r) {
        final ThreadPoolExecutor pool = TASK_POOL;
        pool.submit(r);
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
            try {
                // 睡眠当前线程,触发上下文切换,让唤醒的线程马上参与竞争
                Thread.sleep(0);
            } catch (InterruptedException e) {
                // do nothing
            }
            listener = null;
            RESULT_MAP.remove(requestId);
        }
    }

    @Override
    public Future<Object> sendFuture(Object message) throws Exception {
        if (message == null) {
            throw new Exception("Message be sent is null.");
        }
        return this.doSendFuture(message);
    }

    @Override
    public Object send(Object message) throws Exception {
        if (message == null) {
            throw new Exception("Message be sent is null.");
        }
        return this.doSend(message);
    }

    @Override
    public void receive(Object message) throws Exception {
        if (message == null) {
            throw new Exception("Message received is null.");
        }
        doReceive(message);
        return;
    }

    /**
     * 由具体的client or server的channel处理具体的发送操作
     * 
     * @param message
     * @return
     */
    protected abstract Object doSend(Object message);

    /**
     * 异步
     * 
     * @param message
     * @return
     */
    protected abstract Future<Object> doSendFuture(Object message);

    /**
     * 由具体的client or server的channel处理具体的接受操作
     * 
     * @param message
     * @throws Exception
     */
    protected abstract void doReceive(Object message) throws Exception;
}
