package cauc;

import Protocol.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerThread implements Runnable {
    private SendTask sendTask;
    private ReceiveTask receiveTask;
    private Socket socket;
    private InetAddress inetAddress;
    private DSP_MainForm DSPMainForm;
    private String userIP;
    private boolean notified;
    private boolean certified;
    public SecretKey symmetricKey;

    private volatile ConcurrentLinkedQueue<BasicProtocol> dataQueue = new ConcurrentLinkedQueue<>();
    private static ConcurrentHashMap<String, Socket> onLineCMU = new ConcurrentHashMap<>();
    private static List<BasicProtocol> protocolList = new ArrayList<>();

    public ServerThread(Socket socket, DSP_MainForm DSPMainForm) throws IOException {
        this.socket = socket;
        this.DSPMainForm = DSPMainForm;
        this.userIP = socket.getInetAddress().getHostAddress();

        ObjectOutputStream oos = new ObjectOutputStream(this.socket.getOutputStream());

        oos.writeObject(DSPMainForm.stateMod);
        oos.flush();
        if (DSPMainForm.stateMod == 1) {
            oos.writeObject(DSPMainForm.CMUDialog.keyPair.getPrivate());
            oos.flush();
            oos.writeObject(DSPMainForm.DSPDialog.certificate);
            oos.flush();
            oos.writeObject(DSPMainForm.passwd);
            oos.flush();
        }
    }

    @Override
    public void run() {
        try {
            JOptionPane.showMessageDialog(DSPMainForm.mainPanel, "有设备接入");
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

    public void stop() {
        if (receiveTask != null) {
            receiveTask.isCancle = true;
            receiveTask.interrupt();
            if (receiveTask.inputStream != null) {
                SocketUtil.closeInputStream(receiveTask.inputStream);
                receiveTask.inputStream = null;
            }
            receiveTask = null;
        }

        if (sendTask != null) {
            sendTask.isCancled = true;
            sendTask.interrupt();
            if (sendTask.outputStream != null) {
                synchronized (sendTask.outputStream) {
                    //防止写数据是停止，写完在停止
                    sendTask.outputStream = null;
                }
            }

            sendTask = null;
        }
    }

    /**
     * 向发送队列中添加报文并激活
     *
     * @param data
     */
    public void addRequest(BasicProtocol data) {
        System.out.println(new String(data.getContentData()));
        if (!isConnected()) {
            return;
        }
        dataQueue.add(data);
        toNotifyAll(dataQueue);
    }

    public void toWaitAll(Object obj) {
        synchronized (obj) {
            try {
                obj.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void toNotifyAll(Object obj) {
        synchronized (obj) {
            obj.notifyAll();
        }
    }

    private boolean isConnected() {
        if (socket.isClosed() || !socket.isConnected()) {
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
    public class ReceiveTask extends Thread {
        private DataInputStream inputStream;
        private boolean isCancle;

        @Override
        public void run() {
            try {
                while (!isCancle) {
                    if (!isConnected()) {
                        isCancle = true;
                        break;
                    }

                    if (inputStream != null) {
                        BasicProtocol receivedProtocol = null;
                        switch (DSPMainForm.stateMod) {
                            case 0: {
                                receivedProtocol = SocketUtil.readFromStream(inputStream, DownlinkProtocol.PROTOCOL_TYPE);
                                break;
                            }
                            case 1: {
                                receivedProtocol = SocketUtil.readFromStream(DSPMainForm.CMUDialog.certificate, DSPMainForm.passwd, inputStream, DownlinkProtocol.PROTOCOL_TYPE);
                                break;
                            }
                            case 2: {
                                receivedProtocol = processProtocol(inputStream);
                                break;
                            }
                        }
                        if (receivedProtocol != null) {
                            int flag = 0;
                            for (BasicProtocol each : protocolList) {
                                if (Arrays.toString(receivedProtocol.getContentData()).equals(Arrays.toString(each.getContentData()))) {
                                    JOptionPane.showMessageDialog(null, "重放！");
                                    flag = 1;
                                }
                            }
                            if (flag == 0) {
                                protocolList.add(receivedProtocol);
                                DSPMainForm.messageListModel.addElement(receivedProtocol);
                            }
                        } else {
                            ServerThread.this.stop();
                            protocolList.clear();
                            DSPMainForm.messageListModel.clear();
                            DSPMainForm.detail.setText("");
                        }
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    private DownlinkProtocol processProtocol(InputStream inputStream) {
        DownlinkProtocol receivedProtocol = (DownlinkProtocol) SocketUtil.readFromStream(inputStream, DownlinkProtocol.PROTOCOL_TYPE, isCertified(), symmetricKey);
        if (receivedProtocol != null) {

            if (!isCertified()) {
                byte[] freeText = receivedProtocol.getFreeText();
                if ("HELLO".equals(new String(freeText))) {
                    setNotified(true);
                    addRequest(Message.uplinkMessage(DSPMainForm, Message.CERTIFICATE));
                } else {
                    byte[] temp = CryptoUtil.decryptSymmetricalKey(DSPMainForm.certificate.getKeyPairDto().getPrivateKey(), receivedProtocol.getFreeText());
                    if (temp != null) {
                        symmetricKey = new SecretKeySpec(temp, "SM4");
                        setCertified(true);
                    }
                }
            }
        }
        return receivedProtocol;
    }

    /**
     * 发送线程
     */
    public class SendTask extends Thread {
        private DataOutputStream outputStream;
        private boolean isCancled;

        @Override
        public void run() {
            while (!isCancled) {
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

    public boolean isNotified() {
        return notified;
    }

    private void setNotified(boolean notified) {
        this.notified = notified;
    }

    public boolean isCertified() {
        return certified;
    }

    public void setCertified(boolean certified) {
        this.certified = certified;

    }
}
