/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.netty;

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

/**2017年4月20日下午3:15:45
 * @author xiaoyu
 * @description  
 */
public class Server {
	
	private int port;
	
	public Server(int port) {
		this.port = port;
	}

	EventLoopGroup boss = new NioEventLoopGroup();
	EventLoopGroup worker = new NioEventLoopGroup();

	public void start() throws InterruptedException {
		ServerBootstrap boot = new ServerBootstrap();
		try {
			boot.group(boss, worker)
			.channel(NioServerSocketChannel.class)
			.handler(new LoggingHandler(LogLevel.DEBUG))
			.childOption(ChannelOption.TCP_NODELAY, true)
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					 ChannelPipeline pipe = ch.pipeline();
					 pipe.addLast(new LengthFieldBasedFrameDecoder(65536,0,0))
					 .addLast(new ServerHandler());
				}
			});
			ChannelFuture f = boot.bind(port).sync();
			f.channel().closeFuture().sync();
			
		}
		finally {
			stop();
		}
	}

	public void stop() {
		boss.shutdownGracefully();
		worker.shutdownGracefully();
	}
}
