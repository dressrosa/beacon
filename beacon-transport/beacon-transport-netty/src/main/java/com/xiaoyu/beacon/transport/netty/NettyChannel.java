/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.transport.netty;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import com.xiaoyu.beacon.common.constant.From;
import com.xiaoyu.beacon.transport.BeaconClientChannel;
import com.xiaoyu.beacon.transport.BeaconClientHandler;
import com.xiaoyu.beacon.transport.BeaconServerChannel;
import com.xiaoyu.beacon.transport.BeaconServerHandler;
import com.xiaoyu.beacon.transport.api.BaseChannel;
import com.xiaoyu.beacon.transport.api.BeaconHandler;
import com.xiaoyu.beacon.transport.support.AbstractBeaconChannel;

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

    /**
     * 收发信息的netty channel
     */
    private Channel channel;

    public NettyChannel(Channel channel) {
        this.channel = channel;
    }

    public static BeaconHandler getChannel(Channel ch, From side) throws Exception {
        final ConcurrentMap<Channel, BeaconHandler> chmap = CHANNEL_MAP;
        BeaconHandler beaHander = chmap.get(ch);
        if (beaHander == null) {
            NettyChannel nc = new NettyChannel(ch);
            if (From.CLIENT == side) {
                BeaconHandler b = new BeaconClientHandler(new BeaconClientChannel(nc));
                chmap.put(ch, (beaHander = b));
            } else {
                BeaconHandler b = new BeaconServerHandler(new BeaconServerChannel(nc));
                chmap.put(ch, (beaHander = b));
            }
        }
        return beaHander;
    }

    /**
     * 这里 1检查是否有失效的channel
     */
    public static void checkUnActive() {
        final ConcurrentMap<Channel, BeaconHandler> chmap = CHANNEL_MAP;
        Iterator<Channel> iter = chmap.keySet().iterator();
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
        final ConcurrentMap<Channel, BeaconHandler> chmap = CHANNEL_MAP;
        BeaconHandler handler = chmap.get(channel);
        if (handler != null) {
            chmap.remove(channel);
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
