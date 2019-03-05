/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.transport.netty;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.core.common.constant.BeaconConstants;
import com.xiaoyu.core.common.constant.From;
import com.xiaoyu.transport.api.Client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 2017年4月20日下午3:05:28
 * 
 * @author xiaoyu
 * @description
 */
public class NettyClient implements Client {

    private static final Logger LOG = LoggerFactory.getLogger("NettyClient");

    private Channel clientChannel;

    private static final int RETRY_TIMES = 10;
    private final String host;
    private final int port;
    private Bootstrap boot;
    private EventLoopGroup worker = new NioEventLoopGroup();
    private final NettyClientHandler handler = new NettyClientHandler();
    private final ChannelInitializer<SocketChannel> initialChannel = new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            final ChannelPipeline pipe = ch.pipeline();
            pipe.addLast("lengthDecoder",
                    new LengthFieldBasedFrameDecoder(BeaconConstants.MAX_LEN, BeaconConstants.LEN_OFFSET,
                            BeaconConstants.INT_LEN))
                    .addLast("beaconDecoder", new NettyDecoder())
                    .addLast("beconEncoder", new NettyEncoder())
                    .addLast("beaconClientHandler", handler);
        }
    };

    public NettyClient(String host, int port) {
        this.port = port;
        this.host = host;
        init();
    }

    public void init() {
        boot = new Bootstrap();
        boot.group(worker)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(initialChannel);
    }

    @Override
    public void start() {
        try {
            connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connect() throws Exception {
        ChannelFuture f = null;
        try {
            f = boot.connect(host, port).syncUninterruptibly();
            Channel channel = f.channel();
            if (this.clientChannel != null) {
                if (!this.clientChannel.isActive()) {
                    NettyChannel.removeChannel(this.clientChannel);
                    this.clientChannel = channel;
                }
            } else {
                this.clientChannel = channel;
            }
        } catch (Exception e) {
            int num = 0;
            // 重连
            while (++num < RETRY_TIMES) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1500);
                    LOG.warn("Connect to server {} failed,retry {} times.", (host + ":" + port), num);
                    f = boot.clone(worker = new NioEventLoopGroup())
                            .connect(host, port)
                            .syncUninterruptibly();
                    Channel channel = f.channel();
                    if (this.clientChannel != null) {
                        if (!this.clientChannel.isActive()) {
                            NettyChannel.removeChannel(this.clientChannel);
                            this.clientChannel = channel;
                        }
                    } else {
                        this.clientChannel = channel;
                    }
                    break;
                } catch (Exception e1) {
                    LOG.error("Connect to server->{} failed in end,retry {} times.", (host + ":" + port), num);
                }
            }
            if (f == null) {
                throw new Exception("Connect to server->" + (host + ":" + port) + " failed,please check.");
            }
        } finally {
            if (f != null && f.cause() != null) {
                this.stop();
            }
        }
    }

    @Override
    public void stop() {
        try {
            if (!worker.isShuttingDown()) {
                worker.shutdownGracefully();
                LOG.info("Close netty client which connected to address->{}:{}", host, port);
            }
        } finally {
            NettyChannel.shutdown();
        }
    }

    @Override
    public Future<Object> sendFuture(Object message) throws Exception {
        if (!clientChannel.isActive()) {
            NettyChannel.checkUnActive();
            // client在server断掉的时候,并没有清除
            // 这里server连上了就相当于重连
            this.connect();
        }
        return NettyChannel.getChannel(clientChannel, From.CLIENT).sendFuture(message);
    }

    @Override
    public Object send(Object message) throws Exception {
        if (!clientChannel.isActive()) {
            NettyChannel.checkUnActive();
            // client在server断掉的时候,并没有清除
            // 这里server连上了就相当于重连
            this.connect();
        }
        return NettyChannel.getChannel(clientChannel, From.CLIENT).send(message);
    }
}
