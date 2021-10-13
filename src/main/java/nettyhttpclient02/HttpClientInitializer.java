package nettyhttpclient02;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

/**
 * Netty 客户端初始化
 * @author junyangwei
 * @date 2021-10-12
 */
public class HttpClientInitializer extends ChannelInitializer<SocketChannel> {
    private HttpClientHandler clientHandler;

    /**
     * 每次通过 Netty 客户端向后端服务发起请求前，
     * 先绑定当前用户侧请求到 Netty 服务端的"通信管道"
     * 目的是为了在后端服务响应后，能够直接将信息响应给用户
     * @param serverCtx 用户侧 - Netty服务端 的通道处理器上下文
     */
    void setClientHandler(ChannelHandlerContext serverCtx) {
        this.clientHandler = new HttpClientHandler(serverCtx);
        System.err.println("#### Netty 客户端处理器绑定新的 用户-Netty服务端 通信通道成功.");
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpClientCodec());
        p.addLast(new HttpObjectAggregator(1024 * 1024));
        p.addLast(clientHandler);
    }
}
