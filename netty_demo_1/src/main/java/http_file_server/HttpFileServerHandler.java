package http_file_server;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.LOCATION;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;
import javax.activation.MimetypesFileTypeMap;


/**
 * @Author: xiaohuan
 * @Date: 2020-01-30 11:46
 */
public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
		private final String url;

    public HttpFileServerHandler(String url) {
			this.url = url;
		}

		/**
		 *   DefaultFullHttpRequest, decodeResult: success)
		 *   GET /src/main/java/netty/ HTTP/1.1
		 *   Host: 127.0.0.1:8081
		 *   Connection: keep-alive
		 *   Cache-Control: max-age=0
		 *   User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36
		 *   Upgrade-Insecure-Requests: 1
		 *   Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*;q=0.8
		 *   Accept-Encoding: gzip, deflate, br
		 *   Accept-Language: zh-CN,zh;q=0.9
		 *   Content-Length: 0
		 *
		 *
		 *   DefaultFullHttpRequest, decodeResult: success)
		 *   GET /favicon.ico HTTP/1.1
		 *   Host: 127.0.0.1:8081
		 *   Connection: keep-alive
		 *   Pragma: no-cache
		 *   Cache-Control: no-cache
		 *   User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.96 Safari/537.36
		 *   Accept: image/webp,image/*,*;q=0.8
		 *   Referer: http://127.0.0.1:8081/src/main/java/netty/
		 *   Accept-Encoding: gzip, deflate, sdch, br
		 *   Accept-Language: zh-CN,zh;q=0.8
		 *   Content-Length: 0
		 *
		 *   这里发现，每次刷新，或者点击都会有两个请求，很郁闷？
		 *   浏览器每次发起请求，都会同时请求一次favicon.ico（本次不讨论浏览器缓存了favicon.ico）
		 *
		 */
		public void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
			// 过滤掉浏览器每次发起请求，都会同时请求一次favicon.ico
			if(request.uri().equals("/favicon.ico")){
				return;
			}
			System.out.println("服务器接受消息"+request);

			// 首先对HTTP请求小弟的解码结果进行判断，如果解码失败，直接构造HTTP 400错误返回。
			if (!request.decoderResult().isSuccess()) {
				sendError(ctx, BAD_REQUEST);
				return;
			}
			// 请求方法：如果不是从浏览器或者表单设置为get请求，构造http 405错误返回
			if (request.method() != GET) {
				sendError(ctx, METHOD_NOT_ALLOWED);
				return;
			}
			// 对请求的的URL进行包装
			final String uri = request.uri();
			// 展开URL分析
			final String path = sanitizeUri(uri);

			// 如果构造的URI不合法，则返回HTTP 403错误
			if (path == null) {
				sendError(ctx, FORBIDDEN);
				return;
			}
			File file = new File(path);
			// 如果文件不存在或者是系统隐藏文件，则构造404 异常返回
			if (file.isHidden() || !file.exists()) {
				sendError(ctx, NOT_FOUND);
				return;
			}
			// 如果文件是目录，则发送目录的链接给客户端浏览器
			if (file.isDirectory()) {
				if (uri.endsWith("/")) {
					sendListing(ctx, file);
				} else {
					sendRedirect(ctx, uri + '/');
				}
				return;
			}
			// 用户在浏览器上第几超链接直接打开或者下载文件，合法性监测
			if (!file.isFile()) {
				sendError(ctx, FORBIDDEN);
				return;
			}

			// IE下才会打开文件，其他浏览器都是直接下载
			// 随机文件读写类以读的方式打开文件
			RandomAccessFile randomAccessFile = null;
			try {
				randomAccessFile = new RandomAccessFile(file, "r");// 以只读的方式打开文件
			} catch (FileNotFoundException fnfe) {
				sendError(ctx, NOT_FOUND);
				return;
			}
			// 获取文件长度，构建成功的http应答消息
			long fileLength = randomAccessFile.length();
			// 在消息头中设置content-length和content type
			HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
			HttpUtil.setContentLength(response, fileLength);
			setContentTypeHeader(response, file);
			// 判断是否是keep-alive
			if (HttpUtil.isKeepAlive(request)) {
				response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			}
			// 发送响应消息
			ctx.write(response);


			ChannelFuture sendFileFuture;
			// 通过netty的ChunkedFile对象直接将文件写入到发送缓冲区，最后为sendFileFeature增加GenericFeatureListener，
			// 如果发送完成，打印“Transfer complete”
			sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile, 0, fileLength, 8192), ctx.newProgressivePromise());
			sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
				@Override
				public void operationProgressed(ChannelProgressiveFuture future,
				                                long progress, long total) {
					if (total < 0) { // total unknown
						System.err.println("Transfer progress: " + progress);
					} else {
						System.err.println("Transfer progress: " + progress + " / "
								+ total);
					}
				}

				@Override
				public void operationComplete(ChannelProgressiveFuture future)
						throws Exception {
					System.out.println("Transfer complete.");
				}
			});

			// 如果使用chunked编码，最后需要发送一个编码结束的空消息体，将LastHttpContent的EMPTY_LAST_CONTENT发送到
			// 缓冲区中，标志所有的消息体已经发送完成。同时调用flush方法将之前在发送缓冲区的消息刷新到SocketChannel中发送给对方。
			ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

			// 如果是非keep-alive的，最后一包消息发送完成之后，服务端要主动关闭链接。
			if (!isKeepAlive(request)) {
				lastContentFuture.addListener(ChannelFutureListener.CLOSE);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	    throws Exception {
			cause.printStackTrace();
			if (ctx.channel().isActive()) {
				sendError(ctx, INTERNAL_SERVER_ERROR);
			}
		}

		private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

		private String sanitizeUri(String uri) {
			try {
				// 使用JDK的URLDecoder进行解码
				uri = URLDecoder.decode(uri, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				try {
					uri = URLDecoder.decode(uri, "ISO-8859-1");
				} catch (UnsupportedEncodingException e1) {
					throw new Error();
				}
			}
			// URL合法性判断
			if (!uri.startsWith(url)) {
				return null;
			}
			if (!uri.startsWith("/")) {
				return null;
			}
			// 将硬编码的文件路径分隔符替换为本地操作系统的文件路径分隔符
			uri = uri.replace('/', File.separatorChar);
			// 对新的uri进行二次合法性校验，如果校验失败则直接返回为空。
			if (uri.contains(File.separator + '.')
					|| uri.contains('.' + File.separator) || uri.startsWith(".")
					|| uri.endsWith(".") || INSECURE_URI.matcher(uri).matches()) {
				return null;
			}
			// 对文件进行拼接，使用当前运行程序所在的工程目录 + URI 构造绝对路径返回
			return System.getProperty("user.dir") + File.separator + uri;
		}

		private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

		/**
		 * 这里是构建了一个html页面返回给浏览器
		 * @param ctx
		 * @param dir
		 */
		private static void sendListing(ChannelHandlerContext ctx, File dir) {
			// 创建成功的HTTP响应消息
			FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
			response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
			// 构造响应消息体，由于需要将响应结果显示在浏览器上，所以采用了HTML的格式
			StringBuilder buf = new StringBuilder();
			String dirPath = dir.getPath();
			buf.append("<!DOCTYPE html>\r\n");
			buf.append("<html><head><title>");
			buf.append(dirPath);
			buf.append(" 目录：");
			buf.append("</title></head><body>\r\n");
			buf.append("<h3>");
			buf.append(dirPath).append(" 目录：");
			buf.append("</h3>\r\n");
			buf.append("<ul>");
			// 此处打印了一个 .. 的链接
			buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
			// 用于展示根目录下的所有文件和文件夹，同时使用超链接标识
			for (File f : dir.listFiles()) {
				if (f.isHidden() || !f.canRead()) {
					continue;
				}
				String name = f.getName();
				if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
					continue;
				}
				buf.append("<li>链接：<a href=\"");
				buf.append(name);
				buf.append("\">");
				buf.append(name);
				buf.append("</a></li>\r\n");
			}
			buf.append("</ul></body></html>\r\n");
			// 分配对应消息的缓冲对象
			ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
			// 将缓冲区中的响应消息存放到HTTP应答消息中，然后释放缓冲区，最后调用writeAndFlush将响应消息发送到缓冲区并刷新到SocketChannel中
			response.content().writeBytes(buffer);
			buffer.release();
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		}

		private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
			FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
			response.headers().set(LOCATION, newUri);
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		}

		private static void sendError(ChannelHandlerContext ctx,
				HttpResponseStatus status) {

			FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
					status, Unpooled.copiedBuffer("Failure: " + status.toString()
					+ "\r\n", CharsetUtil.UTF_8));
			response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		}

		private static void setContentTypeHeader(HttpResponse response, File file) {

			MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
			response.headers().set(CONTENT_TYPE,
					mimeTypesMap.getContentType(file.getPath()));
		}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		messageReceived(ctx, request);
	}
}
