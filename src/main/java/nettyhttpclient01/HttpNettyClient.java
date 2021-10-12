package nettyhttpclient01;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.net.URI;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 一个 HTTP 客户端
 *  - 以一种非常简单的形式，发送 HTTP 请求
 * 参考：
 *  - https://netty.io/4.0/xref/io/netty/example/http/snoop/HttpSnoopClient.html
 *  - https://netty.io/4.0/xref/io/netty/example/http/snoop/package-summary.html
 *  - https://www.infoq.cn/article/jd-netty
 * @author junyangwei
 * @date 2021-10-12
 */
public class HttpNettyClient {
    /**
     * 定义要监听 URL（提取IP和端口）
     */
    static final String URL = System.getProperty("url", "http://127.0.0.1:8080/");

    /**
     * HTTP 客户端启动器
     */
    public static void main(String[] args) throws Exception {
        // 获取 uri，并提取它的 schema 以及 host
        URI uri = new URI(URL);
        String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
        String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();

        // 提取 uri 的端口，若没有则校验 scheme，若是 http 端口就为80，若是https 端口就为443
        int port = uri.getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("https".equalsIgnoreCase(scheme)) {
                port = 443;
            }
        }

        // 校验 scheme 是否合法
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            System.err.println("仅支持 HTTP(S).");
            return;
        }

        // 在必要情况下，配置 SSL 支持 HTTPS 的请求
        final boolean ssl = "https".equalsIgnoreCase(scheme);
        // 获取 SSL 证书内容（使用 netty 封装的方法）
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        // 配置客户端
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new HttpNettyClientInitializer(sslCtx));

            // 尝试进行连接（这里是要尝试连接服务端指定 host 和 port）
            Channel ch = b.connect(host, port).sync().channel();

            // 准备发送 HTTP 请求
            HttpRequest request = new DefaultFullHttpRequest(
                    HTTP_1_1, HttpMethod.GET, uri.getRawPath());
            request.headers().set(HttpHeaderNames.HOST, uri.getRawPath());
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);

            // 设置示例的 cookies
            request.headers().set(
                    HttpHeaderNames.COOKIE,
                    ClientCookieEncoder.LAX.encode(
                            new DefaultCookie("my-cookie", "foo"),
                            new DefaultCookie("another-cookie", "bar")));

            // 发送 HTTP 请求
            ch.writeAndFlush(request);

            // 等待服务端响应并且关闭连接
            ch.closeFuture().sync();
        } finally {
            // 关闭执行器线程，退出
            group.shutdownGracefully();
        }
    }
}
