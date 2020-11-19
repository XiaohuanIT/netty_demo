package time_client_2;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * @Author: xiaohuan
 * @Date: 2020-01-21 14:07
 */
public class TimeClient {
	private final String host;
	private final int port;

	public TimeClient(String host, int port) {
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
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() { // 在创建 Channel 时 向 ChannelPipeline 中添加一个 Echo- ClientHandler 实例
						@Override
						public void initChannel(SocketChannel ch){
							ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
							ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
							ch.pipeline().addLast(new StringDecoder());
							ch.pipeline().addLast(new TimeClientHandler());
						}
					});
			// 连接到远程节点，阻塞等待直到连接完成。发起异步连接操作
			ChannelFuture f = b.connect(host, port).sync();
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
		new TimeClient(host, port).start();
	}
}
