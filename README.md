# 目录说明
- src
    - main
        - java
            - httpserver 使用Java的JDK实现的简单HTTP服务
            - nettyhttpserver01 是以Netty作为HTTP服务端的简单实践
            - nettyhttpclient01 是以Netty作为HTTP客户端的简单实践
            - nettyhttpserver02 和 nettyhttpclient02 是以Netty作为服务端，调用Netty客户端将请求发给后端服务，再响应给用户的实践
        - resources
            - test01-0.0.1-SHAPSHOT.jar 是一个简单的 SpringBoot 的Hello World包，启动端口是8801，包含 /test01api/get 的GET接口

# 特别说明
1. 启动 nettyhttpserver02 包中 HttpNettyServer类的main方法前
    - 需先启动 test01 项目
    - `java -jar src/main/resources/test01-0.0.1-SNAPSHOT.jar`
    - 确保访问 http://127.0.0.1:8800/test01api/get 路径是，能够在 Netty 服务端，将请求正确地转发给后端服务 test01
2. nettyhttpserver02 包中的 Netty 服务端示例启动端口为 8800
3. test01 jar 启动端口为 8801
4. nettyhttpserver02 测试
    - 按照上面步骤先启动 test01 jar包，再启动 nettyhttpserver02 包 HttpNettyServer 的 main 方法启动服务端
    - 访问服务端测试地址：http://127.0.0.1:8800/
    - 访问 test01 GET接口地址：http://127.0.0.1:8801/test01api/get
    - 访问服务端代理地址：http://127.0.0.1:8800/test01api/get
    - 访问地址校验不通过(过滤器)地址：http://127.0.0.1:8800/xxx
