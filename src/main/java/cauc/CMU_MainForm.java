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
    public JTextField port;
    public JButton connect;
    public JButton sendMessage;
    public JButton closeCMU;
    public JButton preview;
    public JLabel stateLabel;
    public ConnectionClient client = null;
    public DefaultListModel<BasicProtocol> messageListModel;
    public PrivateKey privateKey;
    public Certificate DSPCertificate;
    public String passwd;
    public ArrayList<String> signValueList = new ArrayList<>();
    public DownlinkProtocol lastProtocol;
    public int stateMod;
    private JLabel mode;
    private JLabel arn;
    private JLabel label;
    private JLabel id;
    private JLabel dubi;
    private JLabel tak;
    private JPanel infoPanel;
    private JList messageList;
    private JTextField ip;
    private JButton replay;
    private final SmiLabelMap mapClass;

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


        //?????????????????????
        sendMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client == null || client.isClosed) {
                    JOptionPane.showMessageDialog(null, "????????????");
                } else {
                    client.addNewRequest(CMU_MainForm.this);
                }
            }
        });

        //?????????????????????
        replay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client == null || client.isClosed) {
                    JOptionPane.showMessageDialog(null, "????????????");
                } else {
                    client.addNewRequest(lastProtocol);
                }
            }
        });

        startServer();

        //?????????????????????
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
        JFrame frame = new JFrame("??????");
        //????????????, this?????????????????????????????????????????? , ??????????????????????????????,?????????????????????????????????
        frame.setContentPane(new Preview(frame, CMU_MainForm.this, stateMod).previewPanel);
        frame.pack();
        frame.setVisible(true);
        frame.setLayout(null);
        //?????????????????????
        frame.setLocation(550, 350);

        //??????????????????
        frame.setResizable(false);
    }

    /**
     * ??????????????????????????????????????????
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
     * ???????????????????????????????????????
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
                        JOptionPane.showMessageDialog(null, "??????????????????");
                    } else if (port2int > 65536 || port2int < 0) {
                        JOptionPane.showMessageDialog(null, "??????????????????");
                    } else {
                        new Thread() {
                            @Override
                            public void run() {
                                client = new ConnectionClient(ip.getText(), port.getText(), CMU_MainForm.this);
                            }
                        }.start();
                    }
                } catch (Exception xe) {
                    JOptionPane.showMessageDialog(null, "??????????????????");
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
        stateLabel.setText("??????????????????????????????");
        messageListModel.clear();
        detail.setText("");
    }

}
