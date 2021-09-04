package cauc;

import Protocol.SelfCertificate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.Certificate;

/**
 * @author jiaxv
 */
public class DNDialog {
    public JPanel SignInfoPanel;
    private JTextField CN;
    private JButton SaveBtn;
    private JButton CloseBtn;
    public JLabel Title;
    private JTextField OU;
    private JTextField O;
    private JTextField L;
    private JTextField ST;
    private JTextField C;
    private JButton ClearBtn;
    private JFrame mainFrame;
    private int mode;
    public KeyPair keyPair;
    public Certificate certificate;
    public JButton fatherButton;

    /**
     * 构造方法
     * @param frame 子窗口（自身）
     */
    public DNDialog(JButton fatherButton, JFrame frame, int mode){
        this.mode = mode;
        this.fatherButton = fatherButton;

        Title.setFont(new Font("Dialog" ,  1 , 20));

        /* 关闭窗口按钮 */
        CloseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });

        /* 保存按钮 */
        SaveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //拼接字符串以便传给X500Name
                String DN = "CN=" + CN.getText() +",OU=" + OU.getText() + ",O=" + O.getText() +
                        ",L=" + L.getText() + ",ST=" + ST.getText() + ",C=" + C.getText();
                try {
                    //生成密钥对并生成证书
                    final KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
                    keyPair = kpg.generateKeyPair();
                    DNDialog.this.certificate = SelfCertificate.genCertificate(keyPair, DN, mode);
                    fatherButton.setEnabled(false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }


                frame.dispose();
            }
        });

        ClearBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearText();
            }
        });
    }

    /**
     * 清空按钮事件
     */
    private void clearText(){
        CN.setText("");
        OU.setText("");
        O.setText("");
        L.setText("");
        ST.setText("");
        C.setText("");
    }
}
