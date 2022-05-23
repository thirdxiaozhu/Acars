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
    private DSP_MainForm DSPMainForm = null;
    private CMU_MainForm CMUMainForm = null;


    public Preview(JFrame frame, DSP_MainForm DSPMainForm, CMU_MainForm CMUMainForm) {
        this.DSPMainForm = DSPMainForm;
        this.CMUMainForm = CMUMainForm;
        Protocol plain = null;
        Protocol cipher = null;
        if (DSPMainForm != null) {
            plain = (DownlinkProtocol) Message.downlinkMessage(CMUMainForm, Message.PREVIEW);
            cipher = (DownlinkProtocol) Message.downlinkMessage(CMUMainForm, Message.ENCRYPT);
        } else {
            plain = (UplinkProtocol) Message.uplinkMessage(DSPMainForm, Message.PREVIEW);
            cipher = (UplinkProtocol) Message.uplinkMessage(DSPMainForm, Message.ENCRYPT);
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
