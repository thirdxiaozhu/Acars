package cauc;

import Protocol.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;

/**
 * @author jiaxv
 */
public class CMU_MainForm {
    public JPanel mainPanel;
    public JTextField modeInput;
    public JTextField arnInput;
    public JTextField labelInput;
    public JTextField idInput;
    public JTextField dubiInput;
    public JTextField takInput;
    public JTextArea detail;
    public JTextArea text;
    private JLabel mode;
    private JLabel arn;
    private JLabel label;
    private JLabel id;
    private JLabel dubi;
    private JLabel tak;
    private JPanel infoPanel;
    public JTextField port;
    public JButton connect;
    public JButton sendMessage;
    public JButton closeCMU;
    public JButton preview;
    public JLabel stateLabel;
    private JList messageList;
    private JTextField ip;
    private JButton replay;
    public ConnectionClient client = null;
    public DefaultListModel<BasicProtocol> messageListModel;
    private SmiLabelMap mapClass;
    public PrivateKey privateKey;
    public Certificate DSPCertificate;
    public String passwd;
    public ArrayList<String> signValueList = new ArrayList<>();
    public DownlinkProtocol lastProtocol;
    public int stateMod;

    public CMU_MainForm() {
        mapClass = new SmiLabelMap();
        lastProtocol = null;
        initPanel();
        initList();
    }

    private void initPanel() {
        //connect.setEnabled(false);
        closeCMU.setEnabled(false);
        infoPanel.setBorder(BorderFactory.createEtchedBorder(0));
        sendMessage.setEnabled(false);
        preview.setEnabled(false);

        stateLabel.setForeground(Color.red);
        detail.setBorder(BorderFactory.createEtchedBorder(0));
        infoPanel.setBorder(BorderFactory.createEtchedBorder(0));


        //发送按钮监听器
        sendMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client == null || client.isClosed) {
                    JOptionPane.showMessageDialog(null, "尚未连接");
                } else {
                    client.addNewRequest(CMU_MainForm.this);
                }
            }
        });

        //发送按钮监听器
        replay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client == null || client.isClosed) {
                    JOptionPane.showMessageDialog(null, "尚未连接");
                } else {
                    client.addNewRequest((DownlinkProtocol) lastProtocol);
                }
            }
        });

        startServer();

        //关闭按钮监听器
        closeCMU.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeEvent();
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
        frame.setContentPane(new Preview(frame, null, CMU_MainForm.this).previewPanel);
        frame.pack();
        frame.setVisible(true);
        frame.setLayout(null);
        //在屏幕中间显示
        frame.setLocation(550, 350);

        //禁止调整大小
        frame.setResizable(false);
    }

    /**
     * 设置报文列表基本内容及触发器
     */
    private void initList() {
        detail.setPreferredSize(new Dimension(200, 100));
        messageListModel = new DefaultListModel<>();
        messageList.setModel(messageListModel);
        messageList.setCellRenderer(new MyListCellRenderer());
        messageList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                UplinkProtocol protocol = (UplinkProtocol) messageList.getSelectedValue();
                if (protocol == null) {
                    return;
                }
                detail.setText(
                        "QU" + " xxxxxxx\n" +
                                ".BSJXXXX " + protocol.getDateTime() + "\n" +
                                mapClass.LABEL_SMI_DOWN.get(Util.getAttributes(protocol.getLabel(), 0)) + "\n" +
                                "AN " + Util.getAttributes(protocol.getArn(), 0) + "\n" +
                                " - " + Util.getAttributes(protocol.getText(), stateMod)
                );
            }
        });
    }

    /**
     * 检查端口是否合法并启动服务
     */
    private void startServer() {
        connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int port2int = Integer.parseInt(port.getText());

                    if (!Util.regularMatch(
                            "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$", ip.getText())) {
                        JOptionPane.showMessageDialog(null, "地址输入错误");
                    } else if (port2int > 65536 || port2int < 0) {
                        JOptionPane.showMessageDialog(null, "端口输入错误");
                    } else {
                        new Thread() {
                            @Override
                            public void run() {
                                client = new ConnectionClient(ip.getText(), port.getText(), CMU_MainForm.this);
                            }
                        }.start();
                    }
                } catch (Exception xe) {
                    JOptionPane.showMessageDialog(null, "端口输入错误");
                }
            }
        });
    }

    public void closeEvent() {
        client.closeConnection();
        port.setEnabled(true);
        closeCMU.setEnabled(false);
        connect.setEnabled(true);
        sendMessage.setEnabled(false);
        preview.setEnabled(false);
        stateLabel.setForeground(Color.red);
        stateLabel.setText("当前连接状态：未启动");
        messageListModel.clear();
        detail.setText("");
    }

}
