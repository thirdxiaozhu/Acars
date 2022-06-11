package cauc;

import Protocol.*;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

import javax.crypto.SecretKey;
import javax.net.SocketFactory;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author jiaxv
 */
public class ConnectionThread implements Runnable {

    private static final List<BasicProtocol> protocolList = new ArrayList<>();
    public SecretKey secretKey;
    protected volatile ConcurrentLinkedQueue<BasicProtocol> dataQueue = new ConcurrentLinkedQueue<>();
    private Socket socket;
    private final String IP;
    private final String port;
    private SendTask sendTask;
    private ReceiveTask receiveTask;
    private final CMU_MainForm CMUMainForm;
    private boolean notified;
    private boolean certified;
    private final byte[] DspId = new byte[10];
    private final byte[] publicKey = new byte[91];
    private boolean isSockAvailable;
    private boolean closeSendTask;

    public ConnectionThread(String ip, String port, CMU_MainForm CMUMainForm) {
        this.IP = ip;
        this.port = port;
        this.CMUMainForm = CMUMainForm;
    }

    @Override
    public void run() {
        try {
            try {
                socket = SocketFactory.getDefault().createSocket(IP, Integer.parseInt(port));

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                //获取私钥和地面站证书
                CMUMainForm.stateMod = (Integer) ois.readObject();
                if (CMUMainForm.stateMod == 1) {
                    CMUMainForm.privateKey = (PrivateKey) ois.readObject();

                    CMUMainForm.DSPCertificate = (Certificate) ois.readObject();
                    CMUMainForm.passwd = (String) ois.readObject();
                } else if (CMUMainForm.stateMod == 2) {
                    addRequest(Message.downlinkMessage(CMUMainForm, Message.HELLO));
                }

                CMUMainForm.port.setEnabled(false);
                CMUMainForm.connect.setEnabled(false);
                CMUMainForm.closeCMU.setEnabled(true);
                CMUMainForm.sendMessage.setEnabled(true);
                CMUMainForm.preview.setEnabled(true);
                CMUMainForm.stateLabel.setText("当前状态：已启动");
                //#008000 ->纯绿色RGB
                CMUMainForm.stateLabel.setForeground(Color.decode("#008000"));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "服务器连接异常，请检查网络");
                e.printStackTrace();
                return;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            isSockAvailable = true;

            //开启接收线程
            receiveTask = new ReceiveTask();
            receiveTask.inputStream = socket.getInputStream();
            receiveTask.start();

            //开启发送线程
            sendTask = new SendTask();
            sendTask.outputStream = socket.getOutputStream();
            sendTask.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 向发送队列中添加报文并激活
     *
     * @param protocol
     */
    public void addRequest(DownlinkProtocol protocol) {
        dataQueue.add(protocol);
        toNotifyAll(dataQueue);
    }

    public synchronized void stop() {
        //关闭接收线程
        closeReceiveTask();

        closeSendTask = true;
        toNotifyAll(dataQueue);

        //关闭socket
        closeSocket();

        //清除数据
        clearData();

        JOptionPane.showMessageDialog(null, "断开连接");
    }

    public void closeReceiveTask() {
        if (receiveTask != null) {
            receiveTask.interrupt();
            receiveTask.isCancle = true;
            if (receiveTask.inputStream != null) {
                try {
                    if (isSockAvailable && !socket.isClosed() && socket.isConnected()) {
                        socket.shutdownInput(); //解决SocketException的问题
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                SocketUtil.closeInputStream(receiveTask.inputStream);
                receiveTask.inputStream = null;
            }
        }
    }

    private void closeSendTask() {
        if (sendTask != null) {
            sendTask.isCancle = true;
            sendTask.interrupt();
            if (sendTask.outputStream != null) {
                synchronized (sendTask.outputStream) {
                    SocketUtil.closeOutputStream(sendTask.outputStream);
                    sendTask.outputStream = null;
                }
            }

            sendTask = null;
        }
    }

    private void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
                isSockAvailable = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void clearData() {
        dataQueue.clear();
    }

    private void toWait(Object o) {
        synchronized (o) {
            try {
                o.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void toNotifyAll(Object o) {
        synchronized (o) {
            o.notifyAll();
        }
    }

    private boolean isConnected() {
        if (socket.isClosed() || !socket.isConnected()) {
            ConnectionThread.this.stop();
            return false;
        }
        return true;
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

    private UplinkProtocol processProtocol(InputStream inputStream) {
        UplinkProtocol receivedProtocol = (UplinkProtocol) SocketUtil.readFromStream(inputStream, UplinkProtocol.PROTOCOL_TYPE, isCertified(), secretKey);
        if (receivedProtocol != null) {
            if (!isNotified()) {
                byte[] pre8bits = new byte[7];
                System.arraycopy(receivedProtocol.getText(), 0, pre8bits, 0, pre8bits.length);
                if ("RESPOND".equals(new String(pre8bits))) {
                    byte[] rawID = new byte[6];
                    System.arraycopy(receivedProtocol.getText(), 7, rawID, 0, rawID.length);

                    if (CMUMainForm.idInput.getText().equals(new String(rawID))) {
                        setNotified(true);
                        System.arraycopy(receivedProtocol.getText(), 13, DspId, 0, DspId.length);
                        System.arraycopy(receivedProtocol.getText(), 23, publicKey, 0, publicKey.length);

                        try {
                            KeyFactory factory = KeyFactory.getInstance("EC");
                            BCECPublicKey pbk = (BCECPublicKey) factory.generatePublic(new X509EncodedKeySpec(publicKey));

                            secretKey = CryptoUtil.getSymmetricalKey();
                            assert secretKey != null;
                            //返回给DSP公钥加密过的对称密钥
                            byte[] temp = CryptoUtil.encryptSymmetricalKey(secretKey.getEncoded(), pbk);
                            addRequest(Message.downlinkMessage(CMUMainForm, temp));
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            } else if (isNotified() && !isCertified()) {
                //第一次获取DSP传来的对称加密内容，Certified还是false，如果成功的话，就变成true，否则还是false
                try {
                    receivedProtocol.setText(CryptoUtil.deCrypt(secretKey, receivedProtocol.getText()));
                    setCertified(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return receivedProtocol;
    }

    /**
     * 接受线程
     */
    public class ReceiveTask extends Thread {
        private boolean isCancle;
        private InputStream inputStream;

        @Override
        public void run() {
            while (!isCancle) {
                if (!isConnected()) {
                    isCancle = true;
                    break;
                }

                if (inputStream != null) {
                    BasicProtocol receivedProtocol = null;
                    switch (CMUMainForm.stateMod) {
                        case 0 -> receivedProtocol = SocketUtil.readFromStream(inputStream, UplinkProtocol.PROTOCOL_TYPE);
                        case 1 -> receivedProtocol = SocketUtil.readFromStream(CMUMainForm.DSPCertificate, CMUMainForm.signValueList, CMUMainForm.passwd, inputStream, UplinkProtocol.PROTOCOL_TYPE);
                        case 2 -> receivedProtocol = processProtocol(inputStream);
                    }
                    if (receivedProtocol != null) {
                        CMUMainForm.messageListModel.addElement(receivedProtocol);
                    } else {
                        ConnectionThread.this.stop();
                        CMUMainForm.closeEvent();
                        break;
                    }
                }
            }
        }
    }

    /**
     * 发送线程
     */
    public class SendTask extends Thread {
        private boolean isCancle = false;
        private OutputStream outputStream;

        @Override
        public void run() {
            while (!isCancle) {
                if (!isConnected()) {
                    break;
                }

                BasicProtocol protocol = dataQueue.poll();
                if (protocol == null) {
                    toWait(dataQueue);
                    if (closeSendTask) {
                        closeSendTask();
                    }
                } else if (outputStream != null) {
                    synchronized (outputStream) {
                        SocketUtil.write2Stream(protocol, outputStream);
                        CMUMainForm.lastProtocol = (DownlinkProtocol) protocol;
                    }
                }
            }

            //循环结束退出输出流
            SocketUtil.closeOutputStream(outputStream);
        }
    }
}
