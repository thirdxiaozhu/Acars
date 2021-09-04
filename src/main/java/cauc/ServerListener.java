package cauc;

import Protocol.DownlinkProtocol;
import Protocol.UplinkProtocol;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author jiaxv
 */
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

            mainForm.port.setEnabled(false);
            mainForm.startDSP.setEnabled(false);
            mainForm.closeDSP.setEnabled(true);
            mainForm.sendMessage.setEnabled(true);
            mainForm.preview.setEnabled(true);
            mainForm.stateLabel.setText("当前连接状态：已启动");
            //#008000 ->纯绿色RGB
            mainForm.stateLabel.setForeground(Color.decode("#008000"));

            System.out.println(String.format("启动成功！端口号%d",port));
            while(isStart){
                Socket socket = serverSocket.accept();
                serverThread = new ServerThread(socket, mainForm);
                if(socket.isConnected()){
                    executorService.execute(serverThread);
                }
            }
            serverSocket.close();
        } catch (BindException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "端口被占用，请重试");
        }catch (SocketException e){
            JOptionPane.showMessageDialog(null, "连接终止");
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
