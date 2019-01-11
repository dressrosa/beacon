/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.transport.netty;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.core.common.constant.BeaconConstants;
import com.xiaoyu.core.common.utils.NetUtil;
import com.xiaoyu.transport.api.Server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 2017年4月
 * 
 * @author xiaoyu
 * @description
 */
public class NettyServer implements Server {

    private static final Logger LOG = LoggerFactory.getLogger(NettyServer.class);

    private final int port;

    private Channel serverChannel;

    public NettyServer(int port) {
        this.port = port;
    }

    private final EventLoopGroup boss = new NioEventLoopGroup();
    private final EventLoopGroup worker = new NioEventLoopGroup();
    private final ServerBootstrap boot = new ServerBootstrap();
    private final NettyServerHandler serverHandler = new NettyServerHandler();
    private final ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            final ChannelPipeline pipe = ch.pipeline();
            pipe
                    .addLast("idleStateHandler",
                            new IdleStateHandler(BeaconConstants.IDLE_READ_TIMEOUT, 0, 0, TimeUnit.SECONDS))
                    .addLast("lengthDecoder",
                            new LengthFieldBasedFrameDecoder(BeaconConstants.MAX_LEN,
                                    BeaconConstants.LEN_OFFSET, BeaconConstants.INT_LEN))
                    .addLast("beaconDecoder", new NettyDecoder())
                    .addLast("beaconEncoder", new NettyEncoder())
                    .addLast("beaconServerHandler", serverHandler);
        }
    };

    @Override
    public void start() {
        bind();
    }

    private void bind() {
        ChannelFuture f = null;
        try {
            boot.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(initializer);
            f = boot.bind(port).syncUninterruptibly();
            Channel channel = f.channel();
            LOG.info("Start netty server at address->{}:{}", NetUtil.localIP(), port);
            if (this.serverChannel != null) {
                if (!this.serverChannel.isActive()) {
                    NettyChannel.removeChannel(this.serverChannel);
                    this.serverChannel = channel;
                }
            } else {
                this.serverChannel = channel;
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
                boss.shutdownGracefully();
                LOG.info("Close netty server at address->{}:{}", NetUtil.localIP(), port);
            }
        } finally {
            NettyChannel.removeChannel(this.serverChannel);
        }
    }
}
