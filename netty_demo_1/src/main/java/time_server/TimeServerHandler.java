package time_server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * 每读到一次消息，就计数一次，然后发送应答消息给客户端。按照设计，服务端接收到的消息总数应该跟客户端发送的消息总数一致相同，
 * 而且请求消息删除回车空行符后应该为"QUERY TIME ORDER"。
 * @Author: xiaohuan
 * @Date: 2020-01-28 10:00
 */

// 在netty5中，是 extends ChannelHandlerAdapter
public class TimeServerHandler extends ChannelInboundHandlerAdapter {
	private int counter;


	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException {
		ByteBuf buf = (ByteBuf) msg;
		byte[] req = new byte[buf.readableBytes()];
		buf.readBytes(req);
		String body = new String(req, "UTF-8").substring(0, req.length - System.getProperty("line.separator").length());
		System.out.println("The time server receive order:" + body + "; the counter is: " + ++counter);
		String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString():"BAD ORDER";
		currentTime = currentTime + System.getProperty("line.separator");
		ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
		ctx.writeAndFlush(resp);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
		System.out.println(cause.toString());
		ctx.close();
	}
}
