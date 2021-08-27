package cauc;

import Protocol.UplinkProtocol;
import Protocol.Util;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Preview {
    public JPanel previewPanel;
    private JTextArea PlainText;
    private JTextArea CipherText;
    private JButton AcceptBtn;
    private final MainForm mainForm;


    public Preview(JFrame frame, MainForm mainForm) {
        this.mainForm = mainForm;
        UplinkProtocol plain = Message.uplinkPreview(mainForm, Message.PREVIEW);
        UplinkProtocol cipher = Message.uplinkPreview(mainForm, Message.UPLINK);
        CipherText.setText(Util.getCypherText(cipher));
        //PlainText.setText(new String(plaintext));
        PlainText.setText(Util.getUntreatedPlainText(plain));

        AcceptBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
    }
}
