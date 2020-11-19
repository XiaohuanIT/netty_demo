package http_server_gzip;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: xiaohuan
 * @Date: 2020-01-26 12:23
 */

@Slf4j
public class HttpGzipServer {
	private final int port;
	private static boolean isSSL;


	public HttpGzipServer(int port) {
		this.port = port;
	}


	public void run() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new HttpGzipServerInitializer());

			Channel ch = b.bind(port).sync().channel();
			System.out.println("HTTP Upload Server at port " + port + '.');
			System.err.println("Open your browser and navigate to http://localhost:" + port + '/');
			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}


	public static void main(String[] args) throws Exception {
		int port;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		} else {
			port = 9000;
		}

		if (args.length > 1) {
			isSSL = true;
		}

		/**
		 * 启动netty服务器
		 */
		log.info("开始启动netty服务器");
		new HttpGzipServer(port).run();
		log.info("netty服务器启动成功");
	}
}
