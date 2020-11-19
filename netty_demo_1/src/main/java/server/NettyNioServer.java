package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * @Author: xiaohuan
 * @Date: 2020-01-21 16:41
 */
public class NettyNioServer {
	private final int port;

	public NettyNioServer(int port){
		this.port = port;
	}

	public static void main(String[] args) throws InterruptedException {
		int port = 9999;
		new EchoServer(port).start();
	}

	public void start() throws InterruptedException {
		final ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hi!\r\n", Charset.forName("UTF-8")));
		final EchoServerHandler serverHandler = new EchoServerHandler();
		// 为非阻塞模式使用NioEventLoopGroup
		EventLoopGroup group = new NioEventLoopGroup();
		try{
			//  创建 Server- Bootstrap
			ServerBootstrap b = new ServerBootstrap();
			b.group(group)
					.channel(NioServerSocketChannel.class)  // 指定所使用的 NIO 传输 Channel
					.localAddress(new InetSocketAddress(port)) // 使用指定的 端口设置套 接字地址
					.childHandler(new ChannelInitializer<SocketChannel>() {  //  指定 Channel- Initializer，对于 每个已接受的 连接都调用它
						@Override
						public void initChannel(SocketChannel ch) {
							//  server.EchoServerHandler 被 标注为@Shareable，所 以我们可以总是使用 同样的实例
							ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
								@Override
								public void channelActive(ChannelHandlerContext ctx) throws Exception {
									ctx.writeAndFlush(buf.duplicate()).addListener(ChannelFutureListener.CLOSE);
								}
							});
						}
					});
			// 异步地绑定服务器; 调用 sync()方法阻塞 等待直到绑定完成
			ChannelFuture f = b.bind().sync();
			// 获取 Channel 的 CloseFuture，并 且阻塞当前线 程直到它完成
			f.channel().closeFuture().sync();
		} finally {
			//  关闭 EventLoopGroup， 释放所有的资源
			group.shutdownGracefully().sync();
		}
	}
}
