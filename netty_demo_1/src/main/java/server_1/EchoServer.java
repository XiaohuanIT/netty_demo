package server_1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class EchoServer {

	public void bind(int port) throws Exception {
		final EchoServerHandler serverHandler = new EchoServerHandler();
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try{
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 100)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast("frameDecoder",new LengthFieldBasedFrameDecoder(65535, 0, 2,0,2));
							ch.pipeline().addLast("msgpack decoder", new MsgPackDecoder());
							ch.pipeline().addLast("frameEncoder",new LengthFieldPrepender(2));
							ch.pipeline().addLast("msgpack encoder", new MsgPackEncoder());
							ch.pipeline().addLast(serverHandler);

						}
					});

			//bind port
			ChannelFuture f = b.bind(port).sync();
			//wait
			f.channel().closeFuture().sync();
		}finally{
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int port = 9000;
		try {
			new EchoServer().bind(port);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
