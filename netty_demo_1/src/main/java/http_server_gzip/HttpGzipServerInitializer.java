package http_server_gzip;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

/**
 * @Author: xiaohuan
 * @Date: 2020-01-26 11:22
 */
public class HttpGzipServerInitializer extends ChannelInitializer<SocketChannel> {
	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		// Create a default pipeline implementation.
		ChannelPipeline pipeline = ch.pipeline();
		//服务端的gizp压缩应在request请求之前
		pipeline.addLast("compressor", new HttpContentCompressor());
		pipeline.addLast("codec", new HttpServerCodec());
		pipeline.addLast("aggegator", new HttpObjectAggregator(512 * 1024));
		pipeline.addLast("handler", new HttpGzipServerHandler());
		/**
		 * http-request解码器
		 * http服务器端对request解码
		 */
		pipeline.addLast("decoder", new HttpRequestDecoder());
		/**
		 * http-response解码器
		 * http服务器端对response编码
		 */
		pipeline.addLast("encoder", new HttpResponseEncoder());

		/**
		 * 压缩
		 * Compresses an HttpMessage and an HttpContent in gzip or deflate encoding
		 * while respecting the "Accept-Encoding" header.
		 * If there is no matching encoding, no compression is done.
		 */
		pipeline.addLast("deflater", new HttpContentCompressor());
	}
}
