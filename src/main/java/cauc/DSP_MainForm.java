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
    public JTextField port;
    public JButton startDSP;
    public JButton sendMessage;
    public JButton closeDSP;
    public JButton preview;
    public JLabel stateLabel;
    public JButton DSPButton;
    public JButton CMUButton;
    public ServerListener serverListener;
    public DefaultListModel<BasicProtocol> messageListModel;
    public DNDialog DSPDialog;
    public DNDialog CMUDialog;
    public String passwd;
    public String ID;
    public SelfCertificate certificate;
    public int stateMod = 0;
    private JLabel mode;
    private JLabel arn;
    private JLabel label;
    private JLabel id;
    private JLabel dubi;
    private JLabel tak;
    private JLabel utc;
    private JPanel infoPanel;
    private JList messageList;
    private JButton noAckMessage;
    private JLabel utcLabel;
    private JPanel SignPanel;
    private JLabel signState;
    private JTextField passwdField;
    private JButton passwdbtn;
    public JComboBox modComboBox;
    private JPanel card;
    private CardLayout cardLayout;
    private JPanel mode1;
    private JPanel mode2;
    private JTextField IDField;
    private JButton CertifButton;
    private JPanel mode0;

    public DSP_MainForm() {
        addCard();
        initPanel();
        initList();
    }

    private void initPanel() {
        startDSP.setEnabled(true);
        closeDSP.setEnabled(false);
        sendMessage.setEnabled(false);
        preview.setEnabled(false);
        detail.setLineWrap(true);

        stateLabel.setForeground(Color.red);
        detail.setBorder(BorderFactory.createEtchedBorder(0));
        infoPanel.setBorder(BorderFactory.createEtchedBorder(0));
        SignPanel.setBorder(BorderFactory.createEtchedBorder(0));

        //?????????????????????
        sendMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (serverListener == null) {
                    JOptionPane.showMessageDialog(null, "????????????");
                } else {
                    serverListener.addNewRequest(DSP_MainForm.this);
                }
            }
        });

        startServer();

        //?????????????????????
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
                    stateLabel.setText("??????????????????????????????");
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
         * ????????????????????????
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
        card.add(mode0, "0");
        card.add(mode1, "1");
        card.add(mode2, "2");
    }

    private void callPreviewLayout() {
        JFrame frame = new JFrame("??????");
        //????????????, this?????????????????????????????????????????? , ??????????????????????????????,?????????????????????????????????
        frame.setContentPane(new Preview(frame, DSP_MainForm.this, stateMod).previewPanel);
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
     * ???????????????????????????????????????
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
                        JOptionPane.showMessageDialog(null, "???????????????1-65535");
                    } else {
                        new Thread() {
                            @Override
                            public void run() {
                                serverListener.start(Integer.parseInt(port.getText()));
                            }
                        }.start();
                    }
                } catch (Exception xe) {
                    JOptionPane.showMessageDialog(null, "??????????????????");
                }
            }
        });
    }

    public void initSignPanel() {
        signState.setForeground(Color.red);
        if (stateMod == 0) {
            initMode0Panel();
        } else if (stateMod == 1) {
            initMode1Panel();
        } else {
            initMode2Panel();
        }
    }

    private void initMode0Panel() {
        startDSP.setEnabled(true);
        signState.setForeground(Color.decode("#008000"));
        signState.setText("??????????????????");
    }

    private void initMode1Panel() {
        startDSP.setEnabled(false);
        signState.setText("??????????????????");
        DSPButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("???????????????");
                DSPDialog = new DNDialog(DSPButton, frame, SelfCertificate.DSP);
                DSPDialog.Title.setText("???????????????");
                //????????????, this?????????????????????????????????????????? , ??????????????????????????????,?????????????????????????????????
                frame.setContentPane(DSPDialog.SignInfoPanel);
                frame.pack();
                frame.setVisible(true);
                frame.setLayout(null);
                //?????????????????????
                frame.setLocation(550, 350);

                //??????????????????
                frame.setResizable(false);

            }
        });
        CMUButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("???????????????");
                CMUDialog = new DNDialog(CMUButton, frame, SelfCertificate.CMU);
                CMUDialog.Title.setText("???????????????");
                //????????????, this?????????????????????????????????????????? , ??????????????????????????????,?????????????????????????????????
                frame.setContentPane(CMUDialog.SignInfoPanel);
                frame.pack();
                frame.setVisible(true);
                frame.setLayout(null);
                //?????????????????????
                frame.setLocation(550, 350);

                //??????????????????
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

        //??????????????????????????????????????????????????????????????????
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (!passwdbtn.isEnabled() && !CMUButton.isEnabled() && !DSPButton.isEnabled()) {
                        startDSP.setEnabled(true);
                        signState.setForeground(Color.decode("#008000"));
                        signState.setText("??????????????????");
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
        startDSP.setEnabled(false);
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


        //??????????????????????????????????????????????????????????????????
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (!CertifButton.isEnabled()) {
                        startDSP.setEnabled(true);
                        signState.setForeground(Color.decode("#008000"));
                        signState.setText("??????????????????");
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
