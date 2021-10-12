package nettyhttpserver01;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 一个 HTTP 服务器
 *  - 以一种非常简单的形式，响应接收到的 HTTP 请求内容
 * 参考：
 *  - https://netty.io/4.0/xref/io/netty/example/http/snoop/HttpSnoopServerHandler.html
 *  - https://netty.io/4.0/xref/io/netty/example/http/snoop/package-summary.html
 * @author junyangwei
 * @date 2021-10-11
 */
public class HttpNettyServerHandler extends SimpleChannelInboundHandler<Object> {

    /**
     * Http 请求
     */
    private HttpRequest request;

    /**
     * 响应内容缓存在 StringBuilder 类对象 buf 中
     */
    private final StringBuilder buf = new StringBuilder();

    /**
     * 结束通道的读取任务后调用
     *  - flush 方法将刷新通道中还未写入的内容，防止下一次 channelRead 时存在遗留的内容
     *    参考：https://stackoverflow.com/questions/52794066/can-anyone-explain-netty-channelhandlercontext-flush
     * @param ctx
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
        // 对请求信息做解析
        if (msg instanceof HttpRequest) {
            // 将 msg 强制转换为 HttpRequest 类对象，获取本次请求
            this.request = (HttpRequest) msg;
            HttpRequest request = this.request;

            // 处理 100 Continue 请求以符合 HTTP 1.1 规范
            if (HttpUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }

            // 重置 buf 长度，并且拼接版本，主机名和请求地址到 buf 中
            buf.setLength(0);
            buf.append("欢迎来到示例网络服务器\r\n");
            buf.append("===================\r\n");

            buf.append("版本：").append(request.protocolVersion()).append("\r\n");
            buf.append("主机名：").append(request.headers().get(HttpHeaderNames.HOST, "未知")).append("\r\n");
            buf.append("请求地址：").append(request.uri()).append("\r\n\r\n");

            // 提取请求头的信息，若不为空则拼接到 buf 中
            HttpHeaders headers = request.headers();
            if (!headers.isEmpty()) {
                for (Map.Entry<String, String> h : headers) {
                    String key = h.getKey();
                    String value = h.getValue();
                    buf.append("请求头：").append(key).append(" = ").append(value).append("\r\n");
                }
                buf.append("\r\n");
            }

            // 解码请求的 uri，并且提取 query 参数，若参数不为空则拼接到 buf 中
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
            Map<String, List<String>> params = queryStringDecoder.parameters();
            if (!params.isEmpty()) {
                for (Map.Entry<String, List<String>> p : params.entrySet()) {
                    String key = p.getKey();
                    List<String> vals = p.getValue();
                    for (String val : vals) {
                        buf.append("参数：").append(key).append(" = ").append(val).append("\r\n");
                    }
                }
                buf.append("\r\n");
            }

            // 拼接解码结果
            appendDecoderResult(buf, request);
        }

        // 对请求体做解析
        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;

            ByteBuf content = httpContent.content();
            if (content.isReadable()) {
                buf.append("内容：");
                buf.append(content.toString(CharsetUtil.UTF_8));
                buf.append("\r\n");
                appendDecoderResult(buf, request);
            }

            if (msg instanceof LastHttpContent) {
                buf.append("内容的结尾\r\n");

                LastHttpContent trailer = (LastHttpContent) msg;
                if (!trailer.trailingHeaders().isEmpty()) {
                    buf.append("\r\n");
                    for (String name: trailer.trailingHeaders().names()) {
                        for (String value : trailer.trailingHeaders().getAll(name)) {
                            buf.append("结尾头：");
                            buf.append(name).append(" = ").append(value).append("\r\n");
                        }
                    }
                    buf.append("\r\n");
                }

                // 写入响应给客户端的响应体，并且获取是否需要保持连接
                boolean keepAlive = writeResponse(trailer, ctx);

                // 若不需要保持连接，则编写完毕后直接关闭连接
                if (!keepAlive) {
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                }
            }

            // 生成ByteBuf对象后，需要调用release方法，防止出现内存泄漏
            // 详情参考：https://www.cnblogs.com/zhaoshizi/p/13122467.html
            content.release();
        }
    }

    /**
     * 拼接当前 HTTP 请求解码的结果
     * @param buf 响应内容缓存的对象
     * @param o HTTP 请求对象
     */
    private static void appendDecoderResult(StringBuilder buf, HttpObject o) {
        DecoderResult result = o.decoderResult();
        if (result.isSuccess()) {
            return;
        }

        buf.append(".. 解码失败：");
        buf.append(result.cause());
        buf.append("\r\n");
    }

    /**
     * 写入响应给客户端的响应体，并且返回是否需要保持连接状态
     * @param currentObj Http 请求对象
     * @param ctx 通道处理器上下文
     * @return 是否需要保持连接状态
     */
    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        // 根据当前请求头信息，决定是否需要保持连接状态
        boolean keepAlive = HttpUtil.isKeepAlive(request);

        // 建立响应对象（响应内容直接使用当前解析的请求头以及请求体）
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, currentObj.decoderResult().isSuccess() ? OK : BAD_REQUEST,
                Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

        // 设置请求头 —— 内容类型为文本，格式为UTF-8
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (keepAlive) {
            // 当 keep-alive 参数开启(保持连接状态），则添加 'Content-Length' 头
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            // 按要求添加保持连接的头
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // 提取请求头中的 cookie，若存在，那么响应头也设置相同的 cookies
        String cookieString = request.headers().get(COOKIE);
        if (cookieString != null) {
            Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(cookieString);

            if (!cookies.isEmpty()) {
                // 在需要的情况下，可以重新设置响应头的 cookie
                for (Cookie cookie : cookies) {
                    response.headers().add(SET_COOKIE, ServerCookieEncoder.LAX.encode(cookie));
                }
            }
        } else {
            // 若请求没有携带 cookie，则可以自定义一些响应的 cookie
            response.headers().add(SET_COOKIE, ServerCookieEncoder.LAX.encode("key1", "value1"));
            response.headers().add(SET_COOKIE, ServerCookieEncoder.LAX.encode("key2", "value2"));
        }

        // 编写响应（最终响应客户端请求的操作）
        // 关于 write 方法可参见：https://stackoverflow.com/questions/52794066/can-anyone-explain-netty-channelhandlercontext-flush
        ctx.write(response);

        return keepAlive;
    }

    /**
     * 处理 100 Continue 请求以符合 HTTP 1.1 规范
     * 详情参见：https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Status/100
     * @param ctx 通道处理器上下文
     */
    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
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

    /**
     * 捕获客户端主动断开连接后，关闭通道
     * @param ctx 通道处理器上下文
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        ctx.close();
    }
}
