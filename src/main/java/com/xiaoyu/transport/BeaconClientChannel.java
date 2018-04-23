package com.xiaoyu.transport;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    private static final int SLEEP_TIME = 1000;
    private static final int RETRY_NUM = 3;
    protected BaseChannel baseChannel;

    public BeaconClientChannel(BaseChannel baseChannel) {
        this.baseChannel = baseChannel;
    }

    @Override
    protected Object doSend(Object message) {
        Future<Object> taskFuture = null;
        try {
            taskFuture = TASK_POOL.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    // 对同一次的请求加锁,当收到结果时释放,这里仅为了使用notify机制
                    Object result = null;
                    final CallbackListener listener = new CallbackListener();
                    synchronized (listener) {
                        addListener(((RpcRequest) message).getId(), listener);
                        // wait次数达到一定限制后(2s内),默认超时.TODO
                        int retry = 0;
                        baseChannel.send(message);
                        /*
                         * 防止发生意外,导致一直阻塞;
                         * 再等待3s后,以超时结束
                         */
                        do {
                            listener.wait(SLEEP_TIME);
                            retry++;
                        } while ((result = listener.result()) == null && retry <= RETRY_NUM);
                        LOG.warn("等待次数:{};时间:{}", retry, SLEEP_TIME * retry);
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
        } finally {

        }
        try {
            return taskFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            return new RpcResponse()
                    .setException(new Exception("error request happened  "))
                    .setId(((RpcRequest) message).getId());
        }
    }

    @Override
    protected void doReceive(Object message) {
        // 对同一此的请求channel加锁,当收到结果时释放
        setResult(((RpcResponse) message).getId(), message);
        // 触发下一个handler的读操作
        // this.channel.pipeline().context("beaconClientHandler").read();
        // try {
        // this.baseChannel.receive(message);
        // } catch (Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
    }

    @Override
    protected Future<Object> doSendFuture(Object message) {
        Future<Object> taskFuture = null;
        try {
            taskFuture = TASK_POOL.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    // 对同一次的请求加锁,当收到结果时释放,这里仅为了使用notify机制
                    Object result = null;
                    final CallbackListener listener = new CallbackListener();
                    synchronized (listener) {
                        addListener(((RpcRequest) message).getId(), listener);
                        // wait次数达到一定限制后(2s内),默认超时.TODO
                        int retry = 0;
                        baseChannel.send(message);
                        /*
                         * 防止发生意外,导致一直阻塞;
                         * 再等待3s后,以超时结束
                         */
                        do {
                            listener.wait(SLEEP_TIME);
                            retry++;
                        } while ((result = listener.result()) == null && retry <= RETRY_NUM);
                        LOG.warn("等待次数:{};时间:{}", retry, SLEEP_TIME * retry);
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
        } finally {

        }
        return taskFuture;
    }

}