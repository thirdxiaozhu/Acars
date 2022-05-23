package cauc;

import Protocol.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * @author jiaxv
 */
public class DSP_MainForm {
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
    public JButton DSPButton;
    public JButton CMUButton;
    private JPanel SignPanel;
    private JLabel signState;
    private JTextField passwdField;
    private JButton passwdbtn;
    private JComboBox modComboBox;
    private JPanel card;
    private CardLayout cardLayout;
    private JPanel mode1;
    private JPanel mode2;
    private JTextField IDField;
    private JButton CertifButton;
    public ServerListener serverListener;
    public DefaultListModel<BasicProtocol> messageListModel;
    public DNDialog DSPDialog;
    public DNDialog CMUDialog;
    public String passwd;
    public String ID;
    public SelfCertificate certificate;
    ;
    public int stateMod = 0;

    public DSP_MainForm() {
        addCard();
        initPanel();
        initList();
    }

    private void initPanel() {
        startDSP.setEnabled(false);
        closeDSP.setEnabled(false);
        sendMessage.setEnabled(false);
        preview.setEnabled(false);
        detail.setLineWrap(true);

        stateLabel.setForeground(Color.red);
        detail.setBorder(BorderFactory.createEtchedBorder(0));
        infoPanel.setBorder(BorderFactory.createEtchedBorder(0));
        SignPanel.setBorder(BorderFactory.createEtchedBorder(0));
        setButtonState(false);
        System.out.println(stateMod);

        //发送按钮监听器
        sendMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (serverListener == null) {
                    JOptionPane.showMessageDialog(null, "尚未连接");
                } else {
                    serverListener.addNewRequest(DSP_MainForm.this);
                }
            }
        });

        startServer();

        //关闭按钮监听器
        closeDSP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    modComboBox.setEnabled(true);
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

        /**
         * 非应答报文监听器
         */
        noAckMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modeInput.setText("");
                arnInput.setText("");
                labelInput.setText("");
                idInput.setText("");
                dubiInput.setText("");
                takInput.setText("");
                key.setText("");
                text.setText("");
                detail.setText("");
                utcLabel.setText("");
            }
        });

        modComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stateMod = modComboBox.getSelectedIndex();
                cardLayout.show(card, String.valueOf(stateMod));
                initSignPanel();
            }
        });
    }

    private void addCard() {
        cardLayout = new CardLayout();
        card.setLayout(cardLayout);
        card.add(mode1, "1");
        card.add(mode2, "2");
    }

    private void callPreviewLayout() {
        JFrame frame = new JFrame("预览");
        //构建窗口, this是父窗口，传入子窗口以便传值 , 同时要传入子窗口自身,以保证实现窗口关闭功能
        frame.setContentPane(new Preview(frame, DSP_MainForm.this, null).previewPanel);
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
                DownlinkProtocol protocol = (DownlinkProtocol) messageList.getSelectedValue();
                if (protocol == null) {
                    return;
                }
                modeInput.setEditable(false);
                arnInput.setEditable(false);
                idInput.setEditable(false);

                modeInput.setText(Util.getAttributes(new byte[]{protocol.getMode()}, 0));
                arnInput.setText(Util.getAttributes(protocol.getArn(), 0));
                idInput.setText(Util.getAttributes(protocol.getFlightId(), stateMod));
                takInput.setText(Util.getAttributes(new byte[]{protocol.getTak()}, 0));
                utcLabel.setText(protocol.getTime());

                detail.setText(
                        "QU" + " xxxxxxx\n" +
                                ".BSJXXXX " + protocol.getDateTime() + "\n" +
                                SmiLabelMap.getInstance().LABEL_SMI_DOWN.get(Util.getAttributes(protocol.getLabel(), 0)) + "\n" +
                                "FI " + Util.getAttributes(protocol.getFlightId(), stateMod) + "/AN " + Util.getAttributes(protocol.getArn(), 0) + "\n" +
                                "DT BJS LOCAL " + protocol.getDateTime() + " M01A\n" +
                                " - " + Util.getAttributes(protocol.getFreeText(), stateMod)
                );
            }
        });
    }

    /**
     * 检查端口是否合法并启动服务
     */
    private void startServer() {
        serverListener = new ServerListener(this);
        startDSP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modComboBox.setEnabled(false);
                try {
                    int port2int = Integer.parseInt(port.getText());

                    if (port2int > 65536 || port2int < 0) {
                        JOptionPane.showMessageDialog(null, "端口范围为1-65535");
                    } else {
                        new Thread() {
                            @Override
                            public void run() {
                                serverListener.start(Integer.parseInt(port.getText()));
                            }
                        }.start();
                    }
                } catch (Exception xe) {
                    JOptionPane.showMessageDialog(null, "端口输入错误");
                }
            }
        });
    }

    public void initSignPanel() {
        signState.setForeground(Color.red);
        if (stateMod == 0) {
            signState.setForeground(Color.decode("#008000"));
            signState.setText("状态：已完成");
            setButtonState(false);
        } else if (stateMod == 1) {
            initMode1Panel();
        } else {
            initMode2Panel();
        }
    }

    void setButtonState(boolean state) {
        startDSP.setEnabled(!state);
        DSPButton.setEnabled(state);
        CMUButton.setEnabled(state);
        passwdbtn.setEnabled(state);
        passwdField.setEnabled(state);
    }

    private void initMode1Panel() {
        signState.setText("状态：未完成");
        setButtonState(true);
        DSPButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("地面站信息");
                DSPDialog = new DNDialog(DSPButton, frame, SelfCertificate.DSP);
                DSPDialog.Title.setText("地面站信息");
                //构建窗口, this是父窗口，传入子窗口以便传值 , 同时要传入子窗口自身,以保证实现窗口关闭功能
                frame.setContentPane(DSPDialog.SignInfoPanel);
                frame.pack();
                frame.setVisible(true);
                frame.setLayout(null);
                //在屏幕中间显示
                frame.setLocation(550, 350);

                //禁止调整大小
                frame.setResizable(false);

            }
        });
        CMUButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("航空器信息");
                CMUDialog = new DNDialog(CMUButton, frame, SelfCertificate.CMU);
                CMUDialog.Title.setText("航空器信息");
                //构建窗口, this是父窗口，传入子窗口以便传值 , 同时要传入子窗口自身,以保证实现窗口关闭功能
                frame.setContentPane(CMUDialog.SignInfoPanel);
                frame.pack();
                frame.setVisible(true);
                frame.setLayout(null);
                //在屏幕中间显示
                frame.setLocation(550, 350);

                //禁止调整大小
                frame.setResizable(false);

            }
        });

        passwdbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                passwd = passwdField.getText();
                passwdField.setEditable(false);
                passwdbtn.setEnabled(false);
            }
        });

        //当证书以及空地协定秘钥均生成后，开放连接选项
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (!passwdbtn.isEnabled() && !CMUButton.isEnabled() && !DSPButton.isEnabled()) {
                        startDSP.setEnabled(true);
                        signState.setForeground(Color.decode("#008000"));
                        signState.setText("状态：已完成");
                        break;
                    }
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void initMode2Panel() {
        SignPanel.setBorder(BorderFactory.createEtchedBorder(0));
        signState.setForeground(Color.red);
        CertifButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ID = IDField.getText();
                certificate = new SelfCertificate(ID);

                CertifButton.setEnabled(false);
            }
        });


        //当证书以及空地协定秘钥均生成后，开放连接选项
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (!CertifButton.isEnabled()) {
                        startDSP.setEnabled(true);
                        signState.setForeground(Color.decode("#008000"));
                        signState.setText("状态：已完成");
                        break;
                    }
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}
