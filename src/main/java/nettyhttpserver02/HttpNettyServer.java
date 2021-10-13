package nettyhttpserver02;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import router01.ApiTagEnum;

import java.net.URI;

/**
 * 一个 HTTP 服务器
 *  - 以一种非常简单的形式，响应接收到的 HTTP 请求内容
 * 参考：
 *  - ../nettyhttpserver01/HttpNettyServer
 * @author junyangwei
 * @date 2021-10-11
 */
public class HttpNettyServer {

    /**
     * 定义 Netty HTTP 服务端启动端口为 8800
     */
    static final int PORT = 8800;

    /**
     * HTTP 服务启动器
     */
    public static void main(String[] args) throws Exception {
        // 定义接收请求以及处理请求的 Reactor 线程池：bossGroup 和 workerGroup
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 构造 ServerBootstrap 实例，ServerBootstrap 是 Netty 服务端的启动辅助类
            ServerBootstrap b = new ServerBootstrap();

            /*
               1. 绑定接收请求的处理器和处理请求的处理器
               2. 绑定服务器Channel，因为 Netty Server 需要创建 NioServerSocketChannel 对象
               3. 绑定日志打印处理器（打印的日志级别大于等于INFO）
               4. 绑定子处理器（自定义的 Netty Server 初始化类）
             */
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpNettyServerInitializer());

            /*
               绑定监听端口并启动服务端，将 NioServerSocketChannel 注册到 Selector 上
               Selector 轮询，由 EventLoop 负责调度和执行 Selector 轮询操作
             */
            URI uri = new URI(ApiTagEnum.DEFAULT.getApiAddress());
            Channel ch = b.bind(uri.getPort()).sync().channel();

            System.err.println("## Netty HTTP 服务端已启动，地址: " + uri);
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
