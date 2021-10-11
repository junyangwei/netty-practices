package httpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 实现一个最简单的 HTTP 服务器（一个请求创建一个线程）
 *  - 访问：http://localhost:8002
 *  - 启动参数：-Xmx512m
 *  - 压测命令：wrk -c 40 -d 30s http://localhost:8002
 * @author junyangwei
 * @date 2021-10-11
 */
public class HttpServer02 {
    public static void main(String[] args) throws IOException {
        // 创建一个绑定 8002 端口的 ServerSocket 类对象
        ServerSocket serverSocket = new ServerSocket(8002);

        // 使用 while 循环，监听来自客户端对 8002 端口的请求
        while (true) {
            try {
                // 使用 ServerSocket 类对象的 accept() 方法，接收客户端的请求
                Socket socket = serverSocket.accept();
                // 客户端的请求过来后，就创建一个新的线程处理这个请求（非常浪费资源）
                Runnable r = () -> HttpCommon.server(socket);
                (new Thread(r)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
