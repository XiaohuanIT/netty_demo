package server_1;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

public class MsgPackEncoder extends MessageToByteEncoder<Object> {
	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
		MessagePack msgPack = new MessagePack();
//      编码，然后转为ButyBuf传递
		byte[] bytes = msgPack.write(o);
		byteBuf.writeBytes(bytes);
	}
}

