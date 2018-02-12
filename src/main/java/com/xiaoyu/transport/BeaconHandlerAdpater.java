package com.xiaoyu.transport;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 继承netty的handler 用于消息的处理
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class BeaconHandlerAdpater extends ChannelHandlerAdapter implements BeaconHandler {

   
    /**
     * 持有client和server在启动时产生的channel引用
     */
    protected Channel channel;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void received(Object msg, BaseChannel beaconChannel) throws Exception {
        if (msg == null) {
            throw new Exception("接受消息为空");
        }
    }

    @Override
    public void stop() {
        // 正常的client或server关闭后,线程池并没有关闭
        // 这里主动通知线程池关闭
        AbstractBeaconChannel.TASK_POOL.shutdown();
    }

    /**
     * @return
     * @throws Exception 
     */
    public BaseChannel getBeaconChannel() throws Exception {
        final Channel ch = this.channel;
        if (ch == null) {
            throw new Exception("no server was alive,please check");
        }
        return AbstractBeaconChannel.getChannel(ch, this);
    }
}
