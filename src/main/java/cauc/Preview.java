package cauc;

import Protocol.Message;
import Protocol.UplinkProtocol;
import Protocol.Util;

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
    private final MainForm mainForm;


    public Preview(JFrame frame, MainForm mainForm) {
        this.mainForm = mainForm;
        UplinkProtocol plain = Message.uplinkMessage(mainForm, Message.PREVIEW);
        UplinkProtocol cipher = Message.uplinkMessage(mainForm, Message.ENCRYPT);
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
