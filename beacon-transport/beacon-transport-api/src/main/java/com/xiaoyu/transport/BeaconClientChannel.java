/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.transport;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.core.common.message.CallbackListener;
import com.xiaoyu.core.common.message.RpcRequest;
import com.xiaoyu.core.common.message.RpcResponse;
import com.xiaoyu.transport.api.BaseChannel;
import com.xiaoyu.transport.support.AbstractBeaconChannel;

/**
 * 客户端的channel
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconClientChannel extends AbstractBeaconChannel {

    private static final Logger LOG = LoggerFactory.getLogger("BeaconClientChannel");

    /**
     * 一次wait的时间(ms)
     */
    private static final int SLEEP_TIME = 5;
    /**
     * 最大尝试次数 最大即50秒
     */
    private static final int MAX_RETRY_NUM = 10_000;

    protected BaseChannel baseChannel;

    public BeaconClientChannel(BaseChannel baseChannel) {
        this.baseChannel = baseChannel;
    }

    @Override
    protected Object doSend(Object message) {
        Future<Object> taskFuture = addTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                // 对同一次的请求加锁,当收到结果时释放
                Object result = null;
                final CallbackListener listener = new CallbackListener();
                synchronized (listener) {
                    addListener(((RpcRequest) message).getId(), listener);
                    // wait次数达到一定限制后,默认超时.TODO
                    int retry = 1;
                    // 发送消息
                    baseChannel.send(message);
                    // 同步等待结果
                    long start = System.currentTimeMillis();
                    // 防止发生意外,导致一直阻塞;再等待一定时间后,以超时结束
                    try {
                        while ((result = listener.result()) == null && retry <= MAX_RETRY_NUM) {
                            // 这里可能会导致最大多等一个SLEEP_TIME
                            listener.wait(SLEEP_TIME);
                            retry++;
                        }
                    } catch (InterruptedException e) {
                        // 外围get超时,执行 taskFuture.cancel(true)进行中断
                        // LOG.debug("Wait for {} times;cost {} ms", retry, (System.currentTimeMillis()
                        // - start));
                        return result;
                    }
                    // LOG.debug("Wait for {} times;cost {} ms", retry, (System.currentTimeMillis()
                    // - start));
                    if (result == null) {
                        // 最大超时
                        result = new RpcResponse()
                                .setException(new TimeoutException(
                                        "Request exceed limit time,cost time->" + (System.currentTimeMillis() - start)))
                                .setId(((RpcRequest) message).getId());
                    }
                }
                // 已获取到结果
                return result;
            }
        });
        try {
            // client设定的超时..
            return taskFuture.get(((RpcRequest) message).getTimeout(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOG.error("" + e);
            // 取消正在执行的thread,否则线程会执行完毕才能结束
            taskFuture.cancel(true);
            return new RpcResponse()
                    .setException(new TimeoutException(
                            "Request failed due to exceed time->" + ((RpcRequest) message).getTimeout() + "ms"))
                    .setId(((RpcRequest) message).getId());

        }
    }

    @Override
    protected void doReceive(Object message) {
        // 对同一次的请求channel加锁,当收到结果时释放
        setResult(((RpcResponse) message).getId(), message);
    }

    @Override
    protected Future<Object> doSendFuture(Object message) {
        Future<Object> taskFuture = this.addTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                // 对同一次的请求加锁,当收到结果时释放
                Object result = null;
                final CallbackListener listener = new CallbackListener();
                synchronized (listener) {
                    addListener(((RpcRequest) message).getId(), listener);
                    // wait次数达到一定限制后,默认超时.TODO
                    int retry = 1;
                    // 发送消息
                    baseChannel.send(message);
                    // 同步获取结果
                    long start = System.currentTimeMillis();
                    // 防止发生意外,导致一直阻塞;再等待一定时间后,以超时结束
                    while ((result = listener.result()) == null && retry <= MAX_RETRY_NUM) {
                        listener.wait(SLEEP_TIME);
                        retry++;
                    }
                    long end;
                    LOG.info("Wait for {} times;cost {} ms", retry, (end = System.currentTimeMillis() - start));
                    if (result == null) {
                        result = new RpcResponse()
                                .setException(new TimeoutException("Request exceed limit time,cost time->" + end))
                                .setId(((RpcRequest) message).getId());
                    }
                }
                // 已获取到结果
                return result;
            }
        });
        return taskFuture;
    }

}