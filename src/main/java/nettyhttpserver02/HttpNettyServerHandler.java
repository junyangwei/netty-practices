package nettyhttpserver02;

import filter01.ProxyBizFilter;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import nettyhttpclient02.HttpClient;
import router01.ApiTagEnum;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 一个 HTTP 服务器
 *  - 以一种非常简单的形式，响应接收到的 HTTP 请求内容
 * 参考：
 *  - ../nettyhttpserver01/HttpNettyServerHandler
 * @author junyangwei
 * @date 2021-10-11
 */
public class HttpNettyServerHandler extends SimpleChannelInboundHandler<Object> {

    /**
     * 连接后端服务 test01Api 的 HTTP 客户端（Netty编写的）
     */
    private HttpClient test01ApiHttpClient;

    /**
     * 代理业务过滤器
     */
    private ProxyBizFilter proxyBizFilter;

    /**
     * Netty HTTP 服务端处理器构造函数
     */
    HttpNettyServerHandler() {
        this.test01ApiHttpClient = new HttpClient(8801, "127.0.0.1");
        this.proxyBizFilter = new ProxyBizFilter();
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

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
    }

    /**
     * 通道的读取操作（获取客户端发送 HTTP 请求到 Netty 服务器的请求）
     * 一个完整的 HTTP 请求将被分为两个部分：
     *  - HttpRequest：请求信息
     *  - HttpContent：请求体
     * @param ctx 通道处理器上下文
     * @param msg 请求体
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;

            System.err.println("## Netty 服务端接收到来自用户的请求:" + request.uri());

            // 过滤器过滤请求 TODO: 可以按需定义过滤器用途
            if (!proxyBizFilter.filter(request, ctx)) {
                this.writeFailResponse(ctx);
                return;
            }

            // TODO: 做路由相关的工作
            if (request.uri().startsWith(ApiTagEnum.TEST01.getApiTag())) {
                test01ApiHttpClient.proxyRequest(ctx, request);
                return;
            }

            // 直接响应
            this.handlerTest(request, ctx);
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
        this.test01ApiHttpClient.close();
    }

    /**
     * 捕获客户端主动断开连接后，关闭通道
     * @param ctx 通道处理器上下文
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        ctx.close();
    }

    /**
     * 处理器测试方法
     * @param fullHttpRequest 完整的 HTTP 请求
     * @param ctx 通道处理器上下文
     */
    private void handlerTest(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {
        FullHttpResponse response = null;
        try {
            String value = "Hello, netty";
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(value.getBytes("UTF-8")));
            response.headers().set("Content-Type", "application/json");
            response.headers().setInt("Content-Length", response.content().readableBytes());
        } catch (Exception e) {
            response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
        } finally {
            if (fullHttpRequest != null) {
                if (!HttpUtil.isKeepAlive(fullHttpRequest)) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    response.headers().set(CONNECTION, KEEP_ALIVE);
                    ctx.write(response);
                }
            }
        }
    }

    /**
     * 写入失败的响应（过滤器过滤失败时调用）
     * @param ctx 通道处理器上下文
     */
    private void writeFailResponse(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
        ctx.writeAndFlush(response);
    }
}
