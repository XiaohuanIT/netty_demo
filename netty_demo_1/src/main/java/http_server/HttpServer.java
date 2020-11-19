package http_server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * @Author: xiaohuan
 * @Date: 2020-01-25 22:13
 */
public class HttpServer {
	private final int port;

	public HttpServer(int port) {
		this.port = port;
	}

	public static void main(String[] args) throws Exception {
		int port;
		if (args.length == 1) {
			port = Integer.parseInt(args[0]);
		}else {
			port = 9000;
		}
		new HttpServer(port).start_1();
	}


	// 这种方式也行
	public void start_1() throws Exception {
		ServerBootstrap b = new ServerBootstrap();
		NioEventLoopGroup group = new NioEventLoopGroup();
		b.group(group)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch)
							throws Exception {
						System.out.println("initChannel ch:" + ch);
						ch.pipeline()
								.addLast("decoder", new HttpRequestDecoder())   // 1
								.addLast("encoder", new HttpResponseEncoder())  // 2
								.addLast("aggregator", new HttpObjectAggregator(512 * 1024))    // 3
								.addLast("handler", new HttpHandler());        // 4
					}
				})
				.option(ChannelOption.SO_BACKLOG, 128) // determining the number of connections queued
				.childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);

		b.bind(port).sync();
	}


	public void start_2() throws Exception {
		ServerBootstrap b = new ServerBootstrap();
		NioEventLoopGroup group = new NioEventLoopGroup();
		try {
			b.group(group)
					.channel(NioServerSocketChannel.class)
					.childHandler(new HttpChannelInitializer(false))
					// determining the number of connections queued
					.option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);
			b.bind(port).sync();
		}
		 finally {
			// 这里需要屏蔽，否则启动后将直接关闭了
			//group.shutdownGracefully();
		}
	}
}
