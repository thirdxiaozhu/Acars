package cauc;

import Protocol.DownlinkProtocol;
import Protocol.UplinkProtocol;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerListener{
    private static boolean isStart;
    private static ServerThread serverThread;
    public MainForm mainForm;
    ExecutorService executorService = Executors.newCachedThreadPool();
    ServerSocket serverSocket = null;

    public ServerListener(MainForm mainForm){
        this.mainForm = mainForm;
    }

    public void start(int port){
        try{
            serverSocket = new ServerSocket(port);
            isStart = true;
            System.out.println(String.format("启动成功！端口号%d",port));
            while(isStart){
                Socket socket = serverSocket.accept();
                serverThread = new ServerThread(socket, mainForm);
                if(socket.isConnected()){
                    executorService.execute(serverThread);
                }
            }

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(serverSocket != null){
                try {
                    isStart = false;
                    serverSocket.close();
                    if(serverThread != null){
                        serverThread.stop();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void closeConnection() throws IOException {
        serverSocket.close();
    }

    /**
     * 发送数据（由于只有一个模拟CMU实例，暂时这样写
     * 如果需要实现多个CMU，那么就在Mainform里使用map来存储键值对，具体参考本人Transporter项目）
     * @param protocol
     */
    public void addNewRequest(UplinkProtocol protocol){
        if(serverThread != null && isStart){
            serverThread.addRequest(protocol);
        }
    }
}
