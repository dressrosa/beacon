/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.transport.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.core.common.constant.BeaconConstants;
import com.xiaoyu.transport.BeaconClientHandler;

import io.netty.bootstrap.Bootstrap;
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
public class NettyClient extends BeaconClientHandler {

    private static final Logger LOG = LoggerFactory.getLogger("NettyClient");

    private final String host;
    private final int port;
    private Bootstrap boot;
    private final EventLoopGroup worker = new NioEventLoopGroup();
    private final NettyClientHandler handler = new NettyClientHandler(this);
    private final ChannelInitializer<SocketChannel> initialChannel = new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            final ChannelPipeline pipe = ch.pipeline();
            pipe.addLast("lengthDecoder", new LengthFieldBasedFrameDecoder(BeaconConstants.MAX_LEN,BeaconConstants.LEN_OFFSET, BeaconConstants.INT_LEN))
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
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(initialChannel);
    }

    public void connect() {
        try {
            ChannelFuture f = boot.connect(host, port).syncUninterruptibly();
            LOG.info("客户端" + host + ":" + port + "->channel:{}",f.channel().id().asLongText());
            f.channel().closeFuture().syncUninterruptibly();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.stop();
        }
    }

    public void stop() {
        worker.shutdownGracefully();
    }

}
