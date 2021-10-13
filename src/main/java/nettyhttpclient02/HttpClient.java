package nettyhttpclient02;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Data;

/**
 * Netty 客户端
 * TODO: 这里有个致命的问题，当连接中途断开时，会出现：java.lang.IllegalStateException: failed to create a child event loop 异常
 *  - 还有高并发场景如何能够保证对象数据的更新正确
 * @author junyangwei
 * @date 2021-10-12
 */
@Data
public class HttpClient {
    /**
     * 定义后端 API 服务开放的端口
     */
    final private int port;

    /**
     * 定义后端 API 服务的 host
     */
    final private String host;

    /**
     * 定义与后端 API 服务的链接通道
     */
    private ChannelFuture apiCh;
    /**
     * 定义处理 IO 的线程池 EventLoopGroup
     */
    private EventLoopGroup group;

    /**
     * 定义 Netty Http 服务端当前调用的通道
     * 目的是知道需要响应请求到哪个用户客户端(通信管道)
     */
    private ChannelHandlerContext serverCtx;

    /**
     * Http 客户端通道配置初始化类
     */
    private HttpClientInitializer clientInitializer;

    public HttpClient(int port, String host) {
        this.port = port;
        this.host = host;
    }

    /**
     * 获取 Netty客户端 与 指定Host及端口的后端服务 的通信通道
     * @param serverCtx 用户侧 与 Netty服务端 的通信通道，用于最终响应用户请求
     */
    public Channel getChannel(ChannelHandlerContext serverCtx) {
        // 校验是否已初始化，已初始化则绑定新的"用户侧与Netty服务端的通信通道"
        if (this.apiCh != null && this.apiCh.channel().isActive()) {
            this.resetClientHandler(serverCtx);
            return this.apiCh.channel();
        }

        // 未初始化则直接进行初始化连接操作，连接后端服务
        try {
            // 重置客户端处理器（绑定的用户侧与Netty服务端通信通道）
            this.resetClientHandler(serverCtx);

            // 连接后端服务
            this.connect();
        } catch (Exception e) {
            e.printStackTrace();
            this.close();
        }
        return this.apiCh.channel();
    }

    /**
     * 连接后端服务
     * @throws Exception
     */
    private void connect() throws Exception {
        // 连接前先检查当前是否已经建立了连接
        if (this.apiCh != null && this.apiCh.channel().isActive()) {
            return;
        }

        // 连接前再检查 EventLoopGroup 类对象是否不为空，不为空则关闭
        if (this.group != null) {
            this.group.shutdownGracefully();
        }

        System.err.println("#### Netty 客户端准备与后端服务建立连接...");

        // 初始化线程池执行器
        this.group = new NioEventLoopGroup(5);

        // 创建一个 ServerBootstrap 类对象，绑定作为启动的入口点
        Bootstrap b = new Bootstrap();
        b.group(group);
        b.channel(NioSocketChannel.class);
        b.handler(clientInitializer);
        b.option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
                .option(ChannelOption.SO_RCVBUF, 1024 * 1024);


        // 尝试进行连接与后端服务
        try {
            this.apiCh = b.connect(host, port).sync();
            System.err.println("#### Netty 客户端与后端服务连接成功，后端服务地址:"
                    + "http://" + this.host + ":" + this.port);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("#### Netty 客户端与后端服务连接失败，原因:" + e.getMessage());
            this.close();
        }
    }

    /**
     * 重置客户端处理器（绑定新的用户与Netty服务端通信通道）
     */
    private void resetClientHandler(ChannelHandlerContext serverCtx) {
        if (clientInitializer == null) {
            clientInitializer = new HttpClientInitializer();
        }
        clientInitializer.setClientHandler(serverCtx);
    }

    /**
     * 转发用户端的请求到服务端
     * @param serverCtx
     * @param request
     */
    public void proxyRequest(ChannelHandlerContext serverCtx, FullHttpRequest request) {
        Channel ch = this.getChannel(serverCtx);
        ch.writeAndFlush(request);
        System.err.println("## Netty 客户端已将请求转发给后端服务:"
                + "http://" + this.host + ":" + this.port + request.uri());
    }

    /**
     * 关闭当前Http客户端连接，并且退出执行器线程
     */
    public void close() {
        if (this.apiCh != null && this.apiCh.channel().isActive()) {
            try {
                this.apiCh.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (this.group != null) {
            this.group.shutdownGracefully();
        }
    }
}
