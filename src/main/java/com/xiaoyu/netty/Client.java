/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 2017年4月20日下午3:05:28
 * 
 * @author xiaoyu
 * @description
 */
public class Client {

	private String host;
	private int port;

	public Client(String host, int port) {
		this.port = port;
		this.host = host;
	}

	EventLoopGroup worker = new NioEventLoopGroup();

	public void start() throws InterruptedException {
		Bootstrap boot  = new Bootstrap();
		
		try {
			boot.group(worker)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 128)
			.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipe = ch.pipeline();
					pipe.addLast(new ClientHandler());
					
				}
			});
			ChannelFuture f = boot.connect(host,port).sync();
			f.channel().closeFuture().sync();
		}
		finally {
			stop();
		}
	}
	
	public  void stop() {
		worker.shutdownGracefully();
	}
}
