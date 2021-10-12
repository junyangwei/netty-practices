package nettyhttpclient01;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * 一个 HTTP 客户端处理器
 * 参考：
 *  - https://netty.io/4.0/xref/io/netty/example/http/snoop/HttpSnoopClientHandler.html
 *  - https://netty.io/4.0/xref/io/netty/example/http/snoop/package-summary.html
 * @author junyangwei
 * @date 2021-10-12
 */
public class HttpNettyClientHandler extends SimpleChannelInboundHandler<HttpObject> {

    /**
     * 结束通道的读取任务后调用
     *  - flush 方法将刷新通道中还未写入的内容，防止下一次 channelRead 时存在遗留的内容
     *    参考：https://stackoverflow.com/questions/52794066/can-anyone-explain-netty-channelhandlercontext-flush
     * @param ctx 通道处理器上下文
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        this.channelRead(ctx, msg);
    }

    /**
     * 通道的读取操作（获取服务端响应的 HTTP 响应体）
     * 一个完整的 HTTP 响应将被分为两个部分：
     *  - HttpResponse：响应信息
     *  - HttpContent：响应内容
     * @param ctx 通道处理器上下文
     * @param msg 请求体
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 对响应信息做解析
        if (msg instanceof HttpResponse) {
            // 将 msg 强制转换为 HttpResponse 类对象，获取响应信息
            HttpResponse response = (HttpResponse) msg;

            // 打印响应的状态码，版本等信息
            System.err.println("状态码：" + response.status());
            System.err.println("版本：" + response.protocolVersion());
            System.err.println();

            // 提取响应头，若不为空则直接打印
            if (!response.headers().isEmpty()) {
                for (String name : response.headers().names()) {
                    for (String value : response.headers().getAll(name)) {
                        System.err.println("响应头: " + name + " = " + value);
                    }
                }
                System.err.println();
            }

            // 检查当前响应体中的传输编码是否被分块（数据以分块的形式发送）
            if (HttpUtil.isTransferEncodingChunked(response)) {
                System.err.println("分块内容 {");
            } else {
                System.err.println("内容 {");
            }
        }

        // 解析响应内容，并打印
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;

            System.err.print(content.content().toString(CharsetUtil.UTF_8));
            System.err.flush();

            if (content instanceof LastHttpContent) {
                System.err.println("} 内容结尾");
                ctx.close();
            }
        }
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
