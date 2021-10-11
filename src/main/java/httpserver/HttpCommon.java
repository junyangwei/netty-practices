package httpserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 最简单的 HTTP 服务公共模块
 * @author junyangwei
 * @date 2021-10-11
 */
public class HttpCommon {
    /**
     * 与客户端通信 server 方法，响应客户端的请求
     * @param socket 服务器创建的 Socket 对象，用于与客户端进行通信
     */
    public static void server(Socket socket) {
        try {
            // 打开双方的 socket 的通道，并且打开一个输入流，模拟向请求客户端响应数据
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            // 输出的就是 HTTP 的报文协议
            printWriter.println("HTTP/1.1 200 OK");
            printWriter.println("Content-Type:text/html;charset=utf-8");
            // 模拟返回一个固定的字符串给客户端
            String body = "Hello, http server!";
            // 再输出一个请求头，显示的告诉客户端当前报文体的长度（用字节数表示）
            // 如果没有这个参数，客户端会不知道整个报文体到哪里是结束，导致出错
            printWriter.println("Content-Length:" + body.getBytes().length);
            // 输出一个空行，隔开"报文头"和"报文体"
            printWriter.println();

            // 写入报文体，并关闭输入流，结束与客户端的通信
            printWriter.write(body);
            printWriter.close();

            // 关闭 socket
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
