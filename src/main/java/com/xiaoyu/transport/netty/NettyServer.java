/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.transport.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.core.common.constant.BeaconConstants;
import com.xiaoyu.transport.BeaconServerHandler;

import io.netty.bootstrap.ServerBootstrap;
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

/**
 * 2017年4月
 * 
 * @author xiaoyu
 * @description
 */
public class NettyServer extends BeaconServerHandler {

    private static final Logger LOG = LoggerFactory.getLogger("NettyServer");

    private final int port;

    public NettyServer(int port) {
        this.port = port;
    }

    final EventLoopGroup boss = new NioEventLoopGroup();
    final EventLoopGroup worker = new NioEventLoopGroup();
    final ServerBootstrap boot = new ServerBootstrap();
    final NettyServerHandler serverHandler = new NettyServerHandler(this);
    final ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            final ChannelPipeline pipe = ch.pipeline();
            pipe
                    .addLast("lengthDecoder",
                            new LengthFieldBasedFrameDecoder(BeaconConstants.MAX_LEN,
                                    BeaconConstants.LEN_OFFSET, BeaconConstants.INT_LEN))
                    .addLast("beaconDecoder", new NettyDecoder())
                    .addLast("beaconEncoder", new NettyEncoder())
                    .addLast("beaconServerHandler", serverHandler);
        }
    };

    public void bind() throws InterruptedException {
        ChannelFuture f = null;
        try {
            boot.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.WARN))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(initializer);
            LOG.info("启动server in port:{}", port);
            f = boot.bind(port).syncUninterruptibly();
            channel = f.channel();
        } finally {
            if (f != null && f.cause() != null) {
                this.stop();
            }
        }
    }

    @Override
    public void stop() {
        // 通知线程池关闭
        super.stop();
        worker.shutdownGracefully();
        boss.shutdownGracefully();
    }
}
