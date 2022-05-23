package cauc;

import Protocol.Message;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author jiaxv
 */
public class ServerListener {
    private static boolean isStart;
    private static ServerThread serverThread;
    public DSP_MainForm DSPMainForm;
    ExecutorService executorService = Executors.newCachedThreadPool();
    ServerSocket serverSocket = null;

    public ServerListener(DSP_MainForm DSPMainForm) {
        this.DSPMainForm = DSPMainForm;
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            isStart = true;

            DSPMainForm.port.setEnabled(false);
            DSPMainForm.startDSP.setEnabled(false);
            DSPMainForm.closeDSP.setEnabled(true);
            DSPMainForm.sendMessage.setEnabled(true);
            DSPMainForm.preview.setEnabled(true);
            DSPMainForm.stateLabel.setText("当前连接状态：已启动");
            //#008000 ->纯绿色RGB
            DSPMainForm.stateLabel.setForeground(Color.decode("#008000"));

            System.out.println(String.format("启动成功！端口号%d", port));
            while (isStart) {
                Socket socket = serverSocket.accept();
                serverThread = new ServerThread(socket, DSPMainForm);
                if (socket.isConnected()) {
                    executorService.execute(serverThread);
                }
            }
            serverSocket.close();
        } catch (BindException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "端口被占用，请重试");
        } catch (SocketException e) {
            JOptionPane.showMessageDialog(null, "连接终止");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    isStart = false;
                    serverSocket.close();
                    if (serverThread != null) {
                        serverThread.stop();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void closeConnection() throws IOException {
        if(serverSocket != null){
            serverSocket.close();
        }
    }

    /**
     * 发送数据（由于只有一个模拟CMU实例，暂时这样写
     * 如果需要实现多个CMU，那么就在Mainform里使用map来存储键值对，具体参考本人Transporter项目）
     */
    public void addNewRequest(DSP_MainForm DSPMainForm) {
        if (serverThread != null && isStart) {
            System.out.println("statemod" + DSPMainForm.stateMod);
            switch (DSPMainForm.stateMod) {
                case 0 -> serverThread.addRequest(Message.uplinkMessage(DSPMainForm, Message.NONE));
                case 1 -> serverThread.addRequest(Message.uplinkMessage(DSPMainForm, Message.ENCRYPT));
                case 2-> serverThread.addRequest(Message.uplinkMessage(DSPMainForm, Message.ENCRYPT_MOD_2, serverThread.symmetricKey));
            }
        }
    }
}
