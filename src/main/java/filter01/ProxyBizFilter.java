package filter01;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import router01.ApiTagEnum;

/**
 * 在接收来自客户端的请求时的过滤器
 * @author junyangwei
 * @date 2021-10-10
 */
public class ProxyBizFilter implements HttpRequestFilter {
    /**
     * 重写filter方法，做一个后端服务白名单过滤器
     *  - 只给：/test01api/ 以及 /test02api/
     */
    @Override
    public boolean filter(FullHttpRequest request, ChannelHandlerContext ctx) {
        String uri = request.uri();

        boolean validUri = uri.startsWith(ApiTagEnum.TEST01.getApiTag())
                || uri.startsWith(ApiTagEnum.TEST02.getApiTag())
                || ApiTagEnum.DEFAULT.getApiTag().equals(uri);

        if (!validUri) {
            System.err.println("####【ERROR】不支持的 uri:" + uri);
            return false;
        }

        request.headers().add("biz-tag", "uri-valid");
        return true;
    }
}
