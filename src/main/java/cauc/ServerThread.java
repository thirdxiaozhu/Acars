package cauc;

import Protocol.BasicProtocol;
import Protocol.SocketUtil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerThread implements Runnable{
    private SendTask sendTask;
    private Socket socket;
    private InetAddress inetAddress;
    private MainForm mainForm;
    private String userIP;

    private volatile ConcurrentLinkedQueue<BasicProtocol> dataQueue = new ConcurrentLinkedQueue<>();
    private static ConcurrentHashMap<String, Socket> onLineCMU = new ConcurrentHashMap<>();

    public ServerThread(Socket socket, MainForm mainForm){
        this.socket = socket;
        this.mainForm = mainForm;
        this.userIP = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
    }

    public void stop(){
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

    public void addMessage(BasicProtocol data){
        if(!isConnected()){
            return;
        }
        dataQueue.offer(data);
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

    public class SendTask extends Thread{
        private DataOutputStream outputStream;
        private boolean isCancled;

        @Override
        public void run() {
            while(!isCancled){
                if(!isConnected()){
                    isCancled = true;
                    break;
                }
            }

            BasicProtocol protocol = dataQueue.poll();
            if(protocol == null){
                toWaitAll(dataQueue);
            }else if(outputStream != null){
                synchronized (outputStream){
                    SocketUtil.write2Stream(protocol ,outputStream);
                }
            }

            SocketUtil.closeOutputStream(outputStream);
        }
    }
}
