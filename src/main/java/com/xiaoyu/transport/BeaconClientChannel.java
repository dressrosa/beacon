package com.xiaoyu.transport;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.core.common.utils.IdUtil;
import com.xiaoyu.core.rpc.message.CallbackListener;
import com.xiaoyu.core.rpc.message.RpcRequest;
import com.xiaoyu.core.rpc.message.RpcResponse;

import io.netty.channel.Channel;

/**
 * 客户端的channel
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconClientChannel extends AbstractBeaconChannel {

    private static final Logger LOG = LoggerFactory.getLogger("BeaconClientChannel");

    private static final int SLEEP_TIME = 200;
    private static final int RETRY_NUM = 10;

    public BeaconClientChannel(Channel ch, BeaconHandler beaconHandler) {
        super(ch, beaconHandler);
    }

    @Override
    protected Future<Object> doSend(Object message) {
        try {
            Future<Object> taskFuture = TASK_POOL.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    // 对同一次的请求channel加锁,当收到结果时释放
                    Object result = null;
                    final CallbackListener listener = new CallbackListener();
                    synchronized (listener) {
                        // 唯一性id
                        String id = IdUtil.requestId();
                        addListener(id, listener);
                        ((RpcRequest) message).setId(id);
                        //刷新消息
                        channel
                                .writeAndFlush(message);
                        // wait次数达到一定限制后(2s内),默认超时.TODO
                        int retry = 0;
                        do {
                            /*
                             * 防止发生意外,导致一直阻塞;
                             * 再等待2s后,以超时结束
                             */
                            listener.wait(SLEEP_TIME);
                            retry++;
                        } while ((result = listener.result()) == null && retry <= RETRY_NUM);
                        LOG.warn("等待次数:{};时间:{}", retry, SLEEP_TIME * retry);
                        if (result == null) {
                            result = new RpcResponse()
                                    .setException(new Exception("request exceed limit time"))
                                    .setId(id);
                        }
                    }
                    // 已获取到结果
                    return result;
                }
            });
            return taskFuture;
        } finally {

        }
    }

    @Override
    protected void doReceive(Object message) {
        // 对同一此的请求channel加锁,当收到结果时释放
        setResult(((RpcResponse) message).getId(), message);
        // 触发下一个handler的读操作
        this.channel.pipeline().context("beaconClientHandler").read();
    }
}