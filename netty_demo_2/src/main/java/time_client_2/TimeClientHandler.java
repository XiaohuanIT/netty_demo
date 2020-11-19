package time_client_2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Author: xiaohuan
 * @Date: 2020-01-21 11:52
 */
// 标记该类的实例可以被 多个 Channel 共享
@ChannelHandler.Sharable
public class TimeClientHandler extends ChannelInboundHandlerAdapter {

	private int counter;
	private byte[] req;

	public TimeClientHandler(){
		req = ("Hi, xiaohuan. Welcome to Netty.$_").getBytes();
	}


	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ByteBuf msg = null;
		for(int i=0; i<100; i++){
			msg = Unpooled.buffer(req.length);
			msg.writeBytes(req);
			ctx.writeAndFlush(msg);
		}

		// 当被通知 Channel 是活跃的时候，发 送一条消息
		//ctx.writeAndFlush(Unpooled.copiedBuffer("channelActive: Netty rocks!", CharsetUtil.UTF_8));
	}


	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		String body =(String)msg;
		System.out.println("Now is :" + body +"; the counter is: " + ++counter);
	}

	// 在发生异常时， 记录错误并关闭 Channel
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}


}
