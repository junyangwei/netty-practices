package nettyhttpclient01;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;

/**
 * Netty 作为 HTTP 客户端的初始化类
 * 参考：
 *  - https://netty.io/4.0/xref/io/netty/example/http/snoop/HttpSnoopClientInitializer.html
 *  - https://netty.io/4.0/xref/io/netty/example/http/snoop/package-summary.html
 * @author junyangwei
 * @date 2021-10-12
 */
public class HttpNettyClientInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * SSL 证书内容（在 HttpNettyServer 中获取）
     */
    private final SslContext sslCtx;

    HttpNettyClientInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    /**
     * 重写初始化通道方法
     * - SocketChannel 是客户端和服务端的通信通道，可从中读取报文，或向其写入报文
     * 扩展：
     * - Group 作为 IO 线程，负责 IO 的读写，就是通过 SocketChannel 的类对象
     *
     * @param ch 通信通道
     */
    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();

        // 在必要情况下，允许 HTTPS
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }

        // 对发送给服务端的 HTTP 请求进行编码
        p.addLast(new HttpRequestEncoder());

        // 对服务端响应回来的 HTTP 响应体进行解码
        p.addLast(new HttpResponseDecoder());

        // 如果不想自动解压内容，就删除下面一行内容
        p.addLast(new HttpContentDecompressor());

        // 如果想要自己处理 Http 分块形式发送的数据，就注释掉下面一行
        p.addLast(new HttpObjectAggregator(1024 * 1024));

        // 添加自定义 Http Netty 客户端处理器
        p.addLast(new HttpNettyClientHandler());
    }
}
