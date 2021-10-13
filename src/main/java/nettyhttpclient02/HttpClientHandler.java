package nettyhttpclient02;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Netty 客户端处理器
 * @author junyangwei
 * @date 2021-10-12
 */
public class HttpClientHandler extends SimpleChannelInboundHandler<HttpObject> {
    final ChannelHandlerContext serverCtx;

    public HttpClientHandler(ChannelHandlerContext serverCtx) {
        this.serverCtx = serverCtx;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 解析后端服务的响应体
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;

            // TODO: 过滤器过滤响应体

            this.serverCtx.writeAndFlush(response);
            System.err.println("#### Netty 客户端已收到后端服务响应，并调用 Netty服务端与用户的通信通道响应用户的请求");
        }
    }

    /**
     * 结束通道的读取任务后调用
     *  - flush 方法将刷新通道中还未写入的内容，防止下一次 channelRead 时存在遗留的内容
     *    参考：https://stackoverflow.com/questions/52794066/can-anyone-explain-netty-channelhandlercontext-flush
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * 异常捕获回调（打印异常，并且关闭通信通道）
     * @param ctx 通道处理器上下文
     * @param cause 异常原因
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
