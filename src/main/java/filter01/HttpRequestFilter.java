package filter01;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * HTTP 请求过滤器接口
 * @author junyangwei
 * @date 2021-10-09
 */
public interface HttpRequestFilter {
    /**
     * 过滤方法
     * @param fullHttpRequest 完整的请求
     * @param ctx netty 的通道处理器的上下文
     * @return true or false 是否过滤通过
     */
    boolean filter(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx);
}
