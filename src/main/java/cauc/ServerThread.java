package cauc;

import Protocol.BasicProtocol;
import Protocol.DownlinkProtocol;
import Protocol.SocketUtil;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerThread implements Runnable{
    private SendTask sendTask;
    private ReceiveTask receiveTask;
    private Socket socket;
    private InetAddress inetAddress;
    private MainForm mainForm;
    private String userIP;

    private volatile ConcurrentLinkedQueue<BasicProtocol> dataQueue = new ConcurrentLinkedQueue<>();
    private static ConcurrentHashMap<String, Socket> onLineCMU = new ConcurrentHashMap<>();

    public ServerThread(Socket socket, MainForm mainForm) throws IOException {
        this.socket = socket;
        this.mainForm = mainForm;
        this.userIP = socket.getInetAddress().getHostAddress();

        ObjectOutputStream oos = new ObjectOutputStream(this.socket.getOutputStream());

        oos.writeObject(mainForm.CMUDialog.keyPair.getPrivate());
        oos.flush();
        oos.writeObject(mainForm.DSPDialog.certificate);
        oos.flush();
        oos.writeObject(mainForm.passwd);
        oos.flush();
    }

    @Override
    public void run() {
        try {
            JOptionPane.showMessageDialog(mainForm.mainPanel, "有设备接入");
            System.out.println("有设备接入");

            //开始接收线程
            receiveTask = new ReceiveTask();
            receiveTask.inputStream = new DataInputStream(socket.getInputStream());
            receiveTask.start();

            //开始发送线程
            sendTask = new SendTask();
            sendTask.outputStream = new DataOutputStream(socket.getOutputStream());
            sendTask.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        if (receiveTask != null) {
            receiveTask.isCancle = true;
            receiveTask.interrupt();
            if (receiveTask.inputStream != null) {
                SocketUtil.closeInputStream(receiveTask.inputStream);
                receiveTask.inputStream = null;
            }
            receiveTask = null;
        }

        if(sendTask != null){
            sendTask.isCancled = true;
            sendTask.interrupt();
            if(sendTask.outputStream != null){
                synchronized (sendTask.outputStream){
                    //防止写数据是停止，写完在停止
                    sendTask.outputStream = null;
                }
            }

            sendTask = null;
        }
    }

    /**
     * 向发送队列中添加报文并激活
     * @param data
     */
    public void addRequest(BasicProtocol data){
        if(!isConnected()){
            return;
        }
        dataQueue.add(data);
        toNotifyAll(dataQueue);
    }

    public void toWaitAll(Object obj){
        synchronized (obj){
            try{
                obj.wait();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public void toNotifyAll(Object obj){
        synchronized (obj){
            obj.notifyAll();
        }
    }

    private boolean isConnected(){
        if(socket.isClosed() || !socket.isConnected()){
            onLineCMU.remove(userIP);
            ServerThread.this.stop();
            System.out.println("socket closed");

            return false;
        }
        return true;
    }

    /**
     * 接受线程
     */
    public class ReceiveTask extends Thread{
        private DataInputStream inputStream;
        private boolean isCancle;

        @Override
        public void run() {
            while(!isCancle){
                if(!isConnected()){
                    isCancle = true;
                    break;
                }

                if(inputStream != null){
                    BasicProtocol receivedProtocol = SocketUtil.readFromStream(mainForm.CMUDialog.certificate, mainForm.passwd, inputStream, DownlinkProtocol.PROTOCOL_TYPE);
                    if(receivedProtocol != null){
                        mainForm.messageListModel.addElement(receivedProtocol);
                    }
                }
            }
        }
    }

    /**
     * 发送线程
     */
    public class SendTask extends Thread{
        private DataOutputStream outputStream;
        private boolean isCancled;

        @Override
        public void run() {
            while(!isCancled) {
                if (!isConnected()) {
                    isCancled = true;
                    break;
                }

                BasicProtocol protocol = dataQueue.poll();
                if (protocol == null) {
                    toWaitAll(dataQueue);
                } else if (outputStream != null) {
                    synchronized (outputStream) {
                        SocketUtil.write2Stream(protocol, outputStream);
                    }
                }
            }

            SocketUtil.closeOutputStream(outputStream);
        }
    }
}
