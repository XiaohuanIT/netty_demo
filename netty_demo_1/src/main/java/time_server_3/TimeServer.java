package time_server_3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @Author: xiaohuan
 * @Date: 2020-01-21 11:35
 */
public class TimeServer {
	private final int port;

	public TimeServer(int port){
		this.port = port;
	}

	public static void main(String[] args) throws InterruptedException {
		int port = 9000;
		new TimeServer(port).start();
	}

	public void start() throws InterruptedException {
		final TimeServerHandler serverHandler = new TimeServerHandler();
		// 创建 EventLoopGroup
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try{
			//  创建 ServerBootstrap
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)  // 指定所使用的 NIO 传输 Channel
					.option(ChannelOption.SO_BACKLOG, 100)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChildChannelHandler());
			// 异步地绑定服务器; 调用 sync()方法阻塞 等待直到绑定完成
			ChannelFuture f = b.bind(port).sync();
			// 获取 Channel 的 CloseFuture，并 且阻塞当前线 程直到它完成
			f.channel().closeFuture().sync();
		} finally {
			//  关闭EventLoopGroup， 释放所有的资源
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	/*
	你使用了一个特殊的类——ChannelInitializer。这是关键。当一个新的连接 被接受时，一个新的子 Channel 将会被创建，
	而 ChannelInitializer 将会把一个你的 server.EchoServerHandler 的实例添加到该 Channel 的 ChannelPipeline 中。
	正如我们之前所 解释的，这个 ChannelHandler 将会收到有关入站消息的通知。
	 */




	private class ChildChannelHandler extends ChannelInitializer<SocketChannel>{

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
			ch.pipeline().addLast(new FixedLengthFrameDecoder(20));
			ch.pipeline().addLast(new StringDecoder());
			ch.pipeline().addLast(new TimeServerHandler());
		}
	}
}
