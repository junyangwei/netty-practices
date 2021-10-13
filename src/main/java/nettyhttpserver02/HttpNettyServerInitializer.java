package nettyhttpserver02;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;

/**
 * Netty 作为 HTTP 服务端的初始化类
 * 参考：
 *  - ../nettyhttpserver01/HttpNettyServerInitializer
 * @author junyangwei
 * @date 2021-10-11
 */
public class HttpNettyServerInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * 重写初始化通道方法
     *  - SocketChannel 是客户端和服务端的通信通道，可从中读取报文，或向其写入报文
     * 扩展：
     *  - Worker Group 作为 IO 线程，负责 IO 的读写就是通过 SocketChannel 的类对象
     *  - Boss Group 作为服务端 Acceptor 线程，用于 accept 客户端链接，并转发给 WorkerGroup 中的线程
     *    作用类似于 HttpServer01/02/03 示例中，循环中调用 ServerSocket 类对象的 accept 方法
     * @param ch 通信通道
     */
    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();

        // 对客户端发送过来的 HTTP 请求进行解码
        p.addLast(new HttpRequestDecoder());

        // 如果想要自己处理 Http 分块形式发送的数据，就注释掉下面一行
        p.addLast(new HttpObjectAggregator(1024 * 1024));

        // 对发送(响应)给客户端的 HTTP 响应体进行编码
        p.addLast(new HttpResponseEncoder());

        // 如果不想自动压缩内容，就删除下面一行内容
        p.addLast(new HttpContentCompressor());

        // 添加自定义 Http Netty 服务端处理器
        p.addLast(new HttpNettyServerHandler());

        System.err.println("## Netty HTTP 服务端初始化与客户端的通信通道成功.");
    }

}
