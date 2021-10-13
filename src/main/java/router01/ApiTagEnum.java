package router01;

import lombok.Getter;

/**
 * 后端 API 标记枚举
 * @author junyangwei
 * @date 2021-10-10
 */
@Getter
public enum ApiTagEnum {
    /**
     * 默认路径
     */
    DEFAULT("/", "http://127.0.0.1:8800/"),
    /**
     * test01 后端服务 API
     */
    TEST01("/test01api", "http://127.0.0.1:8801/"),
    /**
     * test02 后端服务 API
     */
    TEST02("/test02api", "http://127.0.0.1:8002/");
    // ...

    /**
     * 后端服务 uri 前缀标记
     */
    private String apiTag;
    /**
     * 后端服务 host
     */
    private String apiAddress;

    ApiTagEnum(String apiTag, String apiAddress) {
        this.apiTag = apiTag;
        this.apiAddress = apiAddress;
    }
}
