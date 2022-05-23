package cauc;

import Protocol.DownlinkProtocol;
import Protocol.Message;

/**
 * @author jiaxv
 */
public class ConnectionClient {
    public boolean isClosed;

    private ConnectionThread connectionThread;

    /**
     * 创建新的线程
     *
     * @param IP
     * @param port
     * @param CMUMainForm
     */
    public ConnectionClient(String IP, String port, CMU_MainForm CMUMainForm) {
        connectionThread = new ConnectionThread(IP, port, CMUMainForm);
        new Thread(connectionThread).start();
    }

    /**
     * 向线程中加入新的请求报文
     */
    public void addNewRequest(CMU_MainForm CMUMainForm) {
        if (connectionThread != null && !isClosed) {
            switch (CMUMainForm.stateMod) {
                case 0 -> connectionThread.addRequest(Message.downlinkMessage(CMUMainForm, Message.NONE));
                case 1 -> connectionThread.addRequest(Message.downlinkMessage(CMUMainForm, Message.ENCRYPT));
                case 2 -> connectionThread.addRequest(Message.downlinkMessage(CMUMainForm, connectionThread.secretKey));
            }
        }
    }

    public void addNewRequest(DownlinkProtocol protocol) {
        connectionThread.addRequest(protocol);
    }

    /**
     * 关闭线程
     */
    public void closeConnection() {
        isClosed = true;
        connectionThread.stop();
    }

}
