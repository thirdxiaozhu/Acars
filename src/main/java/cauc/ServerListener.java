package cauc;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerListener{
    private static boolean isStart = true;
    private static ServerThread serverThread;
    public MainForm mainForm;

    public ServerListener(MainForm mainForm){
        this.mainForm = mainForm;
    }

    public void start(int port){
        ServerSocket serverSocket = null;
        ExecutorService executorService = Executors.newCachedThreadPool();
        try{
            serverSocket = new ServerSocket(port);
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
                    serverThread.stop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
