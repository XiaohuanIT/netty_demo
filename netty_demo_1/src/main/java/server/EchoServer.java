package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetSocketAddress;

/**
 * @Author: xiaohuan
 * @Date: 2020-01-21 11:35
 */
public class EchoServer {
	private final int port;

	public EchoServer(int port){
		this.port = port;
	}

	public static void main(String[] args) throws InterruptedException {
		int port = 9000;
		new EchoServer(port).start();
	}

	public void start() throws InterruptedException {
		final EchoServerHandler serverHandler = new EchoServerHandler();
		// 创建 EventLoopGroup
		EventLoopGroup group = new NioEventLoopGroup();
		try{
			//  创建 ServerBootstrap
			ServerBootstrap b = new ServerBootstrap();
			b.group(group)
					.channel(NioServerSocketChannel.class)  // 指定所使用的 NIO 传输 Channel
					.localAddress(new InetSocketAddress(port)) // 使用指定的端口设置套接字地址
					.childHandler(new ChannelInitializer<SocketChannel>() {  // 添加一个 server.EchoServer- Handler 到子 Channel 的 ChannelPipeline
						@Override
						public void initChannel(SocketChannel ch) {
							// server.EchoServerHandler 被标注为@Shareable，所以我们可以总是使用同样的实例
							ch.pipeline().addLast(serverHandler);
						}
					});
			// 异步地绑定服务器; 调用 sync()方法阻塞 等待直到绑定完成
			ChannelFuture f = b.bind().sync();
			// 获取 Channel 的 CloseFuture，并 且阻塞当前线 程直到它完成
			f.channel().closeFuture().sync();
		} finally {
			//  关闭EventLoopGroup， 释放所有的资源
			group.shutdownGracefully().sync();
		}
	}

	/*
	你使用了一个特殊的类——ChannelInitializer。这是关键。当一个新的连接被接受时，一个新的子Channel将会被创建，
	而 ChannelInitializer 将会把一个你的 server.EchoServerHandler 的实例添加到该 Channel 的 ChannelPipeline 中。
	正如我们之前所 解释的，这个 ChannelHandler 将会收到有关入站消息的通知。
	 */
}
