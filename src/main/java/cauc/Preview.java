package cauc;

import Protocol.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author jiaxv
 */
public class Preview {
    public JPanel previewPanel;
    private JTextArea PlainText;
    private JTextArea CipherText;
    private JButton AcceptBtn;
    private Protocol plain = null;
    private Protocol cipher = null;


    public Preview(JFrame frame, DSP_MainForm DSPMainForm, int stateMod) {
        plain = Message.uplinkMessage(DSPMainForm, Message.NONE);
        if(stateMod == 1){
            cipher = Message.uplinkMessage(DSPMainForm, Message.ENCRYPT);
        }else {
            cipher = Message.uplinkMessage(DSPMainForm, Message.ENCRYPT_MOD_2, DSPMainForm.serverListener.getServerThread().symmetricKey);
        }
        CipherText.setText(Util.getCypherText(cipher));
        PlainText.setText(Util.getUntreatedPlainText(plain));

        AcceptBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
    }

    public Preview(JFrame frame, CMU_MainForm CMUMainForm, int stateMod) {
        plain = Message.downlinkMessage(CMUMainForm, Message.NONE);
        if(stateMod == 1){
            cipher = Message.downlinkMessage(CMUMainForm, Message.ENCRYPT);
        }else {
            cipher = Message.downlinkMessage(CMUMainForm, Message.ENCRYPT_MOD_2, null, CMUMainForm.client.getConnectionThread().secretKey);
        }
        CipherText.setText(Util.getCypherText(cipher));
        PlainText.setText(Util.getUntreatedPlainText(plain));

        AcceptBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
    }
}
