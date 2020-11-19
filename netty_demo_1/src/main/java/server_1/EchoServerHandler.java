package server_1;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.List;

@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("服务器接收msgpack消息 : "+msg+"");

		/*
		//类MsgPackDecoder中decode方法，如果是 list.add(msgPack.read(bytes))
		//那么就用下面的方法，只能够获取到值
		List<Object> users = (List<Object>)msg;
		for (Object user : users) {
			System.out.println("属性：" + user);
		}
		*/


		// 类MsgPackDecoder中decode方法，解析时候用了User类，所以这里可以强制转换成功。
		User user = (User)msg;
		System.out.println(user.toString());

		// 原路返回给客户端
		ctx.writeAndFlush(msg); //注意，后面不能加 .addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		System.out.println("----channelReadComplete----");
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}

