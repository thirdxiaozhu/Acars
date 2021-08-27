package cauc;

import Protocol.BasicProtocol;
import Protocol.DownlinkProtocol;
import Protocol.Util;

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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class MainForm {
    public JPanel mainPanel;
    public JTextField modeInput;
    public JTextField arnInput;
    public JTextField labelInput;
    public JTextField idInput;
    public JTextField dubiInput;
    public JTextField takInput;
    public JTextField utcInput;
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
    private JButton startDSP;
    private JButton sendMessage;
    private JList messageList;
    private JButton closeDSP;
    private JButton preview;
    private JLabel stateLabel;
    private JButton noAckMessage;
    private ServerListener serverListener;
    public DefaultListModel<BasicProtocol> messageListModel;

    public MainForm(){
        initPanel();
    }

    private void startServer(){
        serverListener = new ServerListener(this);
        startDSP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int port2int = Integer.parseInt(port.getText());

                if(port2int > 65536 || port2int < 0){
                    JOptionPane.showMessageDialog(null, "端口输入错误");
                }else{
                    port.setEnabled(false);
                    startDSP.setEnabled(false);
                    closeDSP.setEnabled(true);
                    stateLabel.setText("当前连接状态：已启动");
                    //#008000 ->纯绿色RGB
                    stateLabel.setForeground(Color.decode("#008000"));
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

    private void initPanel(){
        startDSP.setEnabled(false);
        closeDSP.setEnabled(false);
        infoPanel.setBorder(BorderFactory.createEtchedBorder(0));

        stateLabel.setForeground(Color.red);
        initList();

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

        startServer();
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

    private void initList(){
        messageListModel = new DefaultListModel<>();
        messageList.setModel(messageListModel);
        messageList.setCellRenderer(new MyListCellRenderer());
        messageList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                DownlinkProtocol protocol = (DownlinkProtocol) messageList.getSelectedValue();
                modeInput.setText(Util.getAttributes(new byte[]{protocol.getMode()}, 0));
                arnInput.setText(Util.getAttributes(protocol.getArn(), 0));
                labelInput.setText(Util.getAttributes(protocol.getLabel(), 0));
                idInput.setText(Util.getAttributes(protocol.getFlightId(), 1));
                dubiInput.setText(Util.getAttributes(new byte[]{protocol.getDubi()}, 0));
                takInput.setText(Util.getAttributes(new byte[]{protocol.getTak()}, 0));
            }
        });
    }

}
