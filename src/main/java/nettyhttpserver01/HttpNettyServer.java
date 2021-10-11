package nettyhttpserver01;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * 一个 HTTP 服务器
 *  - 以一种非常简单的形式，响应接收到的 HTTP 请求内容
 * 参考：
 *  - https://netty.io/4.0/xref/io/netty/example/http/snoop/HttpSnoopServer.html
 *  - https://netty.io/4.0/xref/io/netty/example/http/snoop/package-summary.html
 *  - https://www.infoq.cn/article/jd-netty
 * @author junyangwei
 * @date 2021-10-11
 */
public class HttpNettyServer {

    /**
     * 获取系统配置信息 ssl，校验当前系统是否有 SSL 证书支持 HTTPS
     */
    static final boolean SSL = System.getProperty("ssl") != null;

    /**
     * 根据 SSL 证书是否存在来指定端口，存在则使用 8443 端口，否则使用 8080 端口
     */
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : "8080"));

    /**
     * HTTP 服务启动器
     */
    public static void main(String[] args) throws Exception {
        // 获取 SSL 证书内容（使用 netty 封装的方法）
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        /*
            设置并绑定 Reactor 线程池，定义 Netty 的 Reactor 线程池 EventLoopGroup，
            EventLoop 负责所有注册到本线程的Channel。
            - Worker Group 作为 IO 线程，负责 IO 的读写（通过 SocketChannel 的类对象）
            - Boss Group 作为服务端 Acceptor 线程，用于 accept 客户端链接，并转发给 WorkerGroup 中的线程
              作用类似于 HttpServer01/02/03 示例中，循环中调用 ServerSocket 类对象的 accept 方法
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 构造 ServerBootstrap 实例，ServerBootstrap 是 Netty 服务端的启动辅助类
            ServerBootstrap b = new ServerBootstrap();

            /*
                这里主要做了几件事说明，以及注意：
                 - 绑定接收请求的处理器和处理请求的处理器
                 - 绑定服务器Channel，因为 Netty Server 需要创建 NioServerSocketChannel 对象
                 - 绑定日志打印处理器（打印的日志级别大于等于INFO）
                 - 绑定子处理器（自定义的 Netty Server 初始化类）

                注意：
                 - TCP 链接建立时创建 ChannelPipeline，ChannelPipeline 本质上是一个负责和执行 ChannelHandler 的职责链
                 - 添加并设置 ChannelHandler，ChannelHandler 将串行的加入 ChannelPipeline 中
             */
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpNettyServerInitializer(sslCtx));

            /*
               绑定监听端口并启动服务端，将 NioServerSocketChannel 注册到 Selector 上
               Selector 轮询，由 EventLoop 负责调度和执行 Selector 轮询操作
             */
            Channel ch = b.bind(PORT).sync().channel();

            System.err.println("打开你的浏览器，并且导航到 " +
                    (SSL ? "https" : "http") + "://127.0.0.1:" + PORT + "/");
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
