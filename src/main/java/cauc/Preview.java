package cauc;

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
        byte[] plaintext = Message.uplinkPreview(mainForm, Message.PREVIEW);
        byte[] ciphertext = Message.uplinkPreview(mainForm, Message.UPLINK);
        CipherText.setText(new String(ciphertext));
        //PlainText.setText(new String(plaintext));
        PlainText.setText(Util.getDecodedPlainText(plaintext));

        AcceptBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
    }
}
