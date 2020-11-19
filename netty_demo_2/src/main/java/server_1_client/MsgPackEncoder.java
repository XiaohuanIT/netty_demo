package server_1_client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

import java.nio.ByteBuffer;

/**
 * @Author: xiaohuan
 * @Date: 2020-01-28 21:40
 */
public class MsgPackEncoder extends MessageToByteEncoder<Object> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		MessagePack messagePack = new MessagePack();
		// serialize
		byte[] raw = messagePack.write(msg);
		out.writeBytes(raw);
	}
}
