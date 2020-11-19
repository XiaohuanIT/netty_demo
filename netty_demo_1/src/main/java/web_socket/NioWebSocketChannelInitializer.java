package web_socket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @Author: xiaohuan
 * @Date: 2020/5/3 11:01
 */
public class NioWebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
	@Override
	protected void initChannel(SocketChannel ch) {
		ch.pipeline().addLast("logging",new LoggingHandler("DEBUG"));//设置log监听器，并且日志级别为debug，方便观察运行流程
		ch.pipeline().addLast("http-codec",new HttpServerCodec());//设置解码器
		ch.pipeline().addLast("aggregator",new HttpObjectAggregator(65536));//聚合器，使用websocket会用到
		ch.pipeline().addLast("http-chunked",new ChunkedWriteHandler());//用于大数据的分区传输
		// 需要指定接收请求的路由；必须使用以ws后缀结尾的uri才能访问
		ch.pipeline().addLast("webcosket-handler", new WebSocketServerProtocolHandler("ws",null, false, 65536, false, false, false));
		ch.pipeline().addLast("handler",new NioWebSocketHandler());//自定义的业务handler

	}
}
