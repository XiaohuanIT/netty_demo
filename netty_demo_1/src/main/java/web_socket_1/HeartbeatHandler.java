package web_socket_1;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartbeatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception{
        log.debug("receive frame");
        ctx.fireChannelRead(frame.retain());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE:
                    log.info("读空闲触发");
                    handleReaderIdle(ctx);
                    break;
                case WRITER_IDLE:
                    log.info("写空闲触发");
                    break;
                case ALL_IDLE:
                    log.info("读写空闲触发");
                    ctx.channel().close();

                default:
                    break;
            }


        }
    }

    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        log.debug("----READER_IDLE---- channel id: " + ctx.channel().id());
        ctx.close();
    }
}
