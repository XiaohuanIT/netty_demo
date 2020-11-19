package http_server_gzip;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import lombok.extern.slf4j.Slf4j;
import java.net.URI;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * @Author: xiaohuan
 * @Date: 2020-01-26 11:23
 */
@Slf4j
public class HttpGzipServerHandler extends SimpleChannelInboundHandler<HttpObject> {
	private HttpMethod method = null;


	private void writeHttpResponse(String responseMsg, ChannelHandlerContext ctx, HttpResponseStatus status){
		// Send response back so the browser won't timeout
		ByteBuf responseBytes = ctx.alloc().buffer();
		responseBytes.writeBytes(responseMsg.getBytes());
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(responseBytes));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN + "; charset=UTF-8");
		response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
		response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

		/**
		 * 注意，这里假设使用
		 * ctx.write(response);
		 * 那么client将不会收到任何返回，并且连接处于一直没有关闭的状态
		 */
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		System.out.println("channelRead0!");
		log.info(msg.getClass().getName());
		if (msg instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) msg;
			URI uri = new URI(request.uri());
			System.err.println("request uri==" + uri.getPath());
			method = request.method();
			if (HttpMethod.POST.equals(method)) {
				log.info("收到POST请求");
			} else {
				String responseString = "{\"code\":\"000001\"}";
				log.info("收到GET请求");
				writeHttpResponse(responseString, ctx, HttpResponseStatus.OK);
			}
		}

		//2 msg是HttpContent
		if (!HttpMethod.GET.equals(method)) {
			if (msg instanceof HttpContent) {
				HttpContent content = (HttpContent) msg;
				ByteBuf buf = content.content();
				//从HttpContent拿到gzip数据流  并通过content方法拿到ByteBuf对象
				try {
					byte[] bytes = new byte[buf.readableBytes()];
					buf.readBytes(bytes);
					String body = GzipUtils.uncompress(bytes);
					//通过gzip解压缩，拿到还原的json串数据
					log.info("接收到数据：" + body);
					/**
					 * 将数据发送到kafka服务器
					 */
					log.info("向kafka服务器发送消息");
					long begin = System.currentTimeMillis();
					long end = System.currentTimeMillis();
					log.info("向kafka发送消息完成");
					log.info("发送共耗时:" + (end - begin));
				} catch (Exception e) {
					log.error(e.toString());
				}
				//buf.release();
				//buf.retain();
				writeHttpResponse("post返回成功!", ctx, HttpResponseStatus.OK);
				//msg.retain(); // ferrybig: fixed bug http://stackoverflow.com/q/34634750/1542723
				//ctx.fireChannelRead(msg);
			}
		}
	}
}
