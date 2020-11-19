package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * @Author: xiaohuan
 * @Date: 2020-01-21 16:10
 */
public class NettyOioServer {
	private final int port;

	public NettyOioServer(int port){
		this.port = port;
	}

	public static void main(String[] args) throws InterruptedException {
		int port = 9999;
		new EchoServer(port).start();
	}

	public void start() throws InterruptedException {
		final ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hi!\r\n", Charset.forName("UTF-8")));
		// 创建 EventLoopGroup
		EventLoopGroup group = new NioEventLoopGroup();
		try{
			//  创建 ServerBootstrap
			ServerBootstrap b = new ServerBootstrap();
			b.group(group)
					.channel(OioServerSocketChannel.class)  // 使用 OioEventLoopGroup 以允许阻塞模式(旧的 I/O)
					.localAddress(new InetSocketAddress(port)) // 使用指定的 端口设置套 接字地址
					.childHandler(new ChannelInitializer<SocketChannel>() {  // 指定 Channel- Initializer，对于 每个已接受的 连接都调用它
						@Override
						public void initChannel(SocketChannel ch) {

							ch.pipeline().addLast(new ChannelInboundHandlerAdapter() { // 添加一个 ChannelInboundHandler- Adapter 以拦截和 处理事件
								@Override
								public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
									//  将消息写到客户端，并添 加 ChannelFutureListener， 以便消息一被写完就关闭 连接
									ctx.writeAndFlush(buf.duplicate()).addListener(ChannelFutureListener.CLOSE);
								}
							});
						}
					});
			// 绑定服务器以接受连接
			ChannelFuture f = b.bind().sync();
			// 获取 Channel 的 CloseFuture，并 且阻塞当前线 程直到它完成
			f.channel().closeFuture().sync();
		} finally {
			//  关闭 EventLoopGroup， 释放所有的资源
			group.shutdownGracefully().sync();
		}
	}
}
