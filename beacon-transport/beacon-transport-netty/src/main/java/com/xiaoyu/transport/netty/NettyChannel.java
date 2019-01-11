/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.transport.netty;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import com.xiaoyu.core.common.constant.From;
import com.xiaoyu.transport.BeaconClientChannel;
import com.xiaoyu.transport.BeaconClientHandler;
import com.xiaoyu.transport.BeaconServerChannel;
import com.xiaoyu.transport.BeaconServerHandler;
import com.xiaoyu.transport.api.BaseChannel;
import com.xiaoyu.transport.api.BeaconHandler;
import com.xiaoyu.transport.support.AbstractBeaconChannel;

import io.netty.channel.Channel;

/**
 * @author hongyu
 * @date 2018-04
 * @description
 */
public class NettyChannel implements BaseChannel {

    /**
     * 每一个channel都会生成对应的BaseChannel,用于对channel的存储
     */
    public static final ConcurrentMap<Channel, BeaconHandler> CHANNEL_MAP = new ConcurrentHashMap<>(16);

    private Channel channel;

    public NettyChannel(Channel channel) {
        this.channel = channel;
    }

    public static BeaconHandler getChannel(Channel ch, From side) throws Exception {
        BeaconHandler beaHander = CHANNEL_MAP.get(ch);
        if (beaHander == null) {
            NettyChannel nc = new NettyChannel(ch);
            if (From.CLIENT == side) {
                BeaconHandler b = new BeaconClientHandler(new BeaconClientChannel(nc));
                CHANNEL_MAP.put(ch, (beaHander = b));
            } else {
                BeaconHandler b = new BeaconServerHandler(new BeaconServerChannel(nc));
                CHANNEL_MAP.put(ch, (beaHander = b));
            }
        }
        return beaHander;
    }

    /**
     * 这里 1检查是否有失效的channel
     */
    public static void checkUnActive() {
        Iterator<Channel> iter = CHANNEL_MAP.keySet().iterator();
        Channel ch = null;
        while (iter.hasNext()) {
            ch = iter.next();
            if (!ch.isActive()) {
                removeChannel(ch);
            }
        }
    }

    /**
     * 2.通知线程池关闭
     */
    public static void shutdown() {
        checkUnActive();
        // 正常的client或server关闭后,线程池并没有关闭
        // 这里主动通知线程池关闭
        AbstractBeaconChannel.notifyCloseTaskPool();
    }

    /**
     * 本地缓存删除,并close掉
     * 
     * @param channel
     */
    public static void removeChannel(Channel channel) {
        BeaconHandler handler = CHANNEL_MAP.get(channel);
        if (handler != null) {
            CHANNEL_MAP.remove(channel);
        }
        channel.close();
    }

    @Override
    public Object send(Object message) {
        channel.writeAndFlush(message);
        return null;
    }

    @Override
    public void receive(Object message) {
        channel.read();
    }

    @Override
    public Future<Object> sendFuture(Object message) throws Exception {
        channel.writeAndFlush(message);
        return null;
    }

}
