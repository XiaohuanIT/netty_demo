package server_client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;

/**
 * @Author: xiaohuan
 * @Date: 2020-01-21 14:07
 */
public class EchoClient {
	private final String host;
	private final int port;

	public EchoClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void start() throws InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			// 创建 Bootstrap
			Bootstrap b = new Bootstrap();
			b.group(group)  //  指定 EventLoopGroup 以 处理客户端事件;需要适 用于 NIO 的实现
					.channel(NioSocketChannel.class) //适用于 NIO 传输的 Channel 类型
					.remoteAddress(new InetSocketAddress(host, port)) // 设置服务器的 InetSocketAddress
					.handler(new ChannelInitializer<SocketChannel>() { // 在创建 Channel 时 向 ChannelPipeline 中添加一个 Echo- ClientHandler 实例
						@Override
						public void initChannel(SocketChannel ch){
							ch.pipeline().addLast(new EchoClientHandler());
						}
					});
			// 连接到远程节点，阻塞等待直到连接完成
			ChannelFuture f = b.connect().sync();
			//  阻塞，直到 Channel 关闭
			f.channel().closeFuture().sync();
		}finally {
			// 关闭线程池并且释放所有的资源
			group.shutdownGracefully().sync();
		}
	}

	public static void main(String[] args) throws Exception {
		// ipv6地址
		String host = "127.0.0.1";
		int port = 9000;
		new EchoClient(host, port).start();
	}
}
