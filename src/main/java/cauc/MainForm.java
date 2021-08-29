package cauc;

import Protocol.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MainForm {
    public JPanel mainPanel;
    public JTextField modeInput;
    public JTextField arnInput;
    public JTextField labelInput;
    public JTextField idInput;
    public JTextField dubiInput;
    public JTextField takInput;
    public JTextField key;
    public JTextArea detail;
    public JTextArea text;
    private JLabel mode;
    private JLabel arn;
    private JLabel label;
    private JLabel id;
    private JLabel dubi;
    private JLabel tak;
    private JLabel utc;
    private JPanel infoPanel;
    public JTextField port;
    public JButton startDSP;
    public JButton sendMessage;
    public JButton closeDSP;
    public JButton preview;
    public JLabel stateLabel;
    private JList messageList;
    private JButton noAckMessage;
    private JLabel utcLabel;
    private ServerListener serverListener;
    public DefaultListModel<BasicProtocol> messageListModel;
    private SmiLabelMap mapClass;

    public MainForm(){
        mapClass = new SmiLabelMap();
        initPanel();
        initList();
    }

    private void initPanel(){
        startDSP.setEnabled(false);
        closeDSP.setEnabled(false);
        sendMessage.setEnabled(false);
        preview.setEnabled(false);

        stateLabel.setForeground(Color.red);
        detail.setBorder(BorderFactory.createEtchedBorder(0));
        infoPanel.setBorder(BorderFactory.createEtchedBorder(0));

        port.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if(e.getDocument() == port.getDocument()){
                    Document doc = e.getDocument();
                    try {
                        if(!"".equals(doc.getText(0, doc.getLength()))){
                            startDSP.setEnabled(true);
                        }
                    } catch (BadLocationException badLocationException) {
                        badLocationException.printStackTrace();
                    }
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if(e.getDocument() == port.getDocument()){
                    Document doc = e.getDocument();
                    try {
                        if("".equals(doc.getText(0, doc.getLength()))){
                            startDSP.setEnabled(false);
                        }
                    } catch (BadLocationException badLocationException) {
                        badLocationException.printStackTrace();
                    }
                }
            }
            @Override
            public void changedUpdate(DocumentEvent e) {}
        });

        //发送按钮监听器
        sendMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(serverListener == null){
                    JOptionPane.showMessageDialog(null, "尚未连接");
                }else {
                    serverListener.addNewRequest(Message.uplinkMessage(MainForm.this, Message.ENCRYPT));
                }
            }
        });

        startServer();

        //关闭按钮监听器
        closeDSP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    serverListener.closeConnection();
                    port.setEnabled(true);
                    closeDSP.setEnabled(false);
                    startDSP.setEnabled(true);
                    sendMessage.setEnabled(false);
                    preview.setEnabled(false);
                    stateLabel.setForeground(Color.red);
                    stateLabel.setText("当前连接状态：未启动");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        preview.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                callPreviewLayout();
            }
        });
    }

    private void callPreviewLayout() {
        JFrame frame = new JFrame("预览");
        //构建窗口, this是父窗口，传入子窗口以便传值 , 同时要传入子窗口自身,以保证实现窗口关闭功能
        frame.setContentPane(new Preview(frame , this).previewPanel);
        frame.pack();
        frame.setVisible(true);
        frame.setLayout(null);
        //在屏幕中间显示
        frame.setLocation(550 , 350);

        //禁止调整大小
        frame.setResizable(false);
    }

    /**
     * 设置报文列表基本内容及触发器
     */
    private void initList(){
        detail.setPreferredSize(new Dimension(200,100));
        messageListModel = new DefaultListModel<>();
        messageList.setModel(messageListModel);
        messageList.setCellRenderer(new MyListCellRenderer());
        messageList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                DownlinkProtocol protocol = (DownlinkProtocol) messageList.getSelectedValue();
                modeInput.setEditable(false);
                arnInput.setEditable(false);
                idInput.setEditable(false);

                modeInput.setText(Util.getAttributes(new byte[]{protocol.getMode()}, 0));
                arnInput.setText(Util.getAttributes(protocol.getArn(), 0));
                idInput.setText(Util.getAttributes(protocol.getFlightId(), 1));
                takInput.setText(Util.getAttributes(new byte[]{protocol.getTak()}, 0));
                utcLabel.setText(protocol.getTime());

                detail.setText(
                        "QU" + " xxxxxxx\n" +
                        ".BSJXXXX " + protocol.getDateTime() + "\n" +
                        mapClass.LABEL_SMI_DOWN.get(Util.getAttributes(protocol.getLabel(), 0)) + "\n" +
                        "FI " + Util.getAttributes(protocol.getFlightId(), 1) + "/AN " + Util.getAttributes(protocol.getArn(), 0) + "\n" +
                        "DT BJS LOCAL " + protocol.getDateTime() + " M01A\n" +
                        " - " + Util.getAttributes(protocol.getFreeText(), 1)
                );
            }
        });
    }

    /**
     * 检查端口是否合法并启动服务
     */
    private void startServer(){
        serverListener = new ServerListener(this);
        startDSP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int port2int = Integer.parseInt(port.getText());

                if(port2int > 65536 || port2int < 0){
                    JOptionPane.showMessageDialog(null, "端口输入错误");
                }else{
                    new Thread(){
                        @Override
                        public void run() {
                            serverListener.start(Integer.parseInt(port.getText()));
                        }
                    }.start();
                }
            }
        });
    }

}
