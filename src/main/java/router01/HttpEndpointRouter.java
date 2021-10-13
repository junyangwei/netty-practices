package router01;

import java.util.List;

/**
 * @author junyangwei
 * @date 2021-10-09
 */
public interface HttpEndpointRouter {

    String route(List<String> endpoints);

    /**
     * 获取 uri 对应的后端服务端口（其实还可以扩展到前端的应用）
     * @param uri 请求的 uri
     * @return 端口字符串
     */
    static String getApiHost(String uri) {
        if (uri.startsWith(ApiTagEnum.TEST01.getApiTag())) {
            return ApiTagEnum.TEST01.getApiAddress();
        } else if (uri.startsWith(ApiTagEnum.TEST02.getApiTag())) {
            return ApiTagEnum.TEST02.getApiAddress();
        } else {
            return ApiTagEnum.DEFAULT.getApiAddress();
        }
    }
}
