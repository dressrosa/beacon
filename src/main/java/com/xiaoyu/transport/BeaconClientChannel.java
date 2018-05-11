package com.xiaoyu.transport;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.core.rpc.message.CallbackListener;
import com.xiaoyu.core.rpc.message.RpcRequest;
import com.xiaoyu.core.rpc.message.RpcResponse;
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
     * 一次wait的时间(ms) 每次等待时间:SLEEP_TIME*2的RETRY_NUM次方
     */
    private static final int SLEEP_TIME = 1;
    /**
     * 最大尝试次数
     */
    private static final int MAX_RETRY_NUM = 13;

    /**
     * future最大等待时间 10s
     */
    private static final int MAX_WAIT_TIME = 5;

    protected BaseChannel baseChannel;

    public BeaconClientChannel(BaseChannel baseChannel) {
        this.baseChannel = baseChannel;
    }

    @Override
    protected Object doSend(Object message) {
        Future<Object> taskFuture = this.addTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                // 对同一次的请求加锁,当收到结果时释放,这里仅为了使用notify机制
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
                    while ((result = listener.result()) == null && retry <= MAX_RETRY_NUM) {
                        listener.wait(SLEEP_TIME << retry);
                        retry++;
                    }
                    LOG.info("等待次数->{};耗时->{}", retry, (System.currentTimeMillis() - start));
                    if (result == null) {
                        result = new RpcResponse()
                                .setException(new Exception("request exceed limit time"))
                                .setId(((RpcRequest) message).getId());
                    }
                }
                // 已获取到结果
                return result;
            }
        });
        try {
            return taskFuture.get(MAX_WAIT_TIME, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return new RpcResponse()
                    .setException(new Exception("request failed."))
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
                // 对同一次的请求加锁,当收到结果时释放,这里仅为了使用notify机制
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
                        listener.wait(SLEEP_TIME * retry);
                        retry++;
                    }
                    LOG.info("等待次数->{};耗时->{}", retry, (System.currentTimeMillis() - start));
                    if (result == null) {
                        result = new RpcResponse()
                                .setException(new Exception("request exceed limit time"))
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