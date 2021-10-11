package httpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 实现一个最简单的 HTTP 服务器（线程池）
 *  - 访问：http://localhost:8003
 *  - 启动参数：-Xmx512m
 *  - 压测命令：wrk -c 40 -d 30s http://localhost:8003
 * @author junyangwei
 * @date 2021-10-11
 */
public class HttpServer03 {
    public static void main(String[] args) throws IOException {
        // 使用 JDK 方法创建一个线程池
        ExecutorService executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() + 2);
        // 创建一个绑定8803端口的ServerSocket类对象
        final ServerSocket serverSocket = new ServerSocket(8003);

        // 在while循环中不断地尝试（单线程）
        while (true) {
            try {
                // 等待客户端的请求过来
                final Socket socket = serverSocket.accept();
                // 当客户端请求过来后，将待处理的请求交给我们的线程池，来异步处理，就不需要每个请求都创建一个线程了
                executorService.execute(() -> HttpCommon.server(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
