package http_server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;

/**
 * @Author: xiaohuan
 * @Date: 2020-01-25 22:16
 */
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> { // 1

	private AsciiString contentType = HttpHeaderValues.TEXT_PLAIN;

	// Please keep in mind that this method will be renamed to messageReceived(ChannelHandlerContext, I) in 5.0.
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
		// Check for invalid http data:
		if(msg.decoderResult() != DecoderResult.SUCCESS ) {
			ctx.close();
			return;
		}
		System.out.println("class:" + msg.getClass().getName());
		System.out.println("Recieved request!");
		System.out.println("HTTP Method: " + msg.method());
		System.out.println("HTTP Version: " + msg.protocolVersion());
		System.out.println("URI: " + msg.uri());
		System.out.println("Headers: " + msg.headers());
		System.out.println("Trailing headers: " + msg.trailingHeaders());

		ByteBuf data = msg.content();
		System.out.println("POST/PUT length: " + data.readableBytes());
		System.out.println("POST/PUT as string: ");
		System.out.println("-- DATA --");
		System.out.println(data.toString(StandardCharsets.UTF_8));
		System.out.println("-- DATA END --");

		// Send response back so the browser won't timeout
		ByteBuf responseBytes = ctx.alloc().buffer();
		responseBytes.writeBytes("Hello World".getBytes());

		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(responseBytes));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType + "; charset="+CharsetUtil.UTF_8.toString());
		response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
		response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		ctx.write(response);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		System.out.println("channelReadComplete");
		super.channelReadComplete(ctx);
		ctx.flush(); // 4
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("exceptionCaught");
		if(null != cause) {
			cause.printStackTrace();
		}
		if(null != ctx) {
			ctx.close();
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		System.out.println("Connected!");
	}
}
