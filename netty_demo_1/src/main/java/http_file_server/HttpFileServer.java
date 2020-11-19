package http_file_server;

import http_server.HttpChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @Author: xiaohuan
 * @Date: 2020-01-30 11:46
 */
public class HttpFileServer {

	// 这里目录要写完整的相对路径，包括main/java
	private static final String DEFAULT_URL = "/src/main/java";

	public void run(final int port, final String url) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch)
								throws Exception {
							ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
							ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
							// 新增HTTP响应编码器，对HTTP响应消息进行编码
							ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
							// 新增Chunked handler，主要作用是支持异步发送大的码流（例如大文件传输），但是不占用过多的内存，防止发生java内存溢出错误
							ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
							// HttpFileServerHandler用于文件服务器的业务逻辑处理
							ch.pipeline().addLast("fileServerHandler", new HttpFileServerHandler(url));
						}
					});
			String host = "127.0.0.1";
			ChannelFuture future = b.bind(host, port).sync();
			System.out.println("HTTP文件目录服务器启动，网址是 : http://" + host +":" + port + url);
			future.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		int port = 9000;
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		String url = DEFAULT_URL;
		if (args.length > 1) {
			url = args[1];
		}
		new HttpFileServer().run(port, url);
	}
}

