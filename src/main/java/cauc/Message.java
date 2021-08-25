package cauc;

import Protocol.BasicProtocol;
import Protocol.CryptoUtil;
import Protocol.UplinkProtocol;
import Protocol.Util;

import javax.crypto.CipherInputStream;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;

/**
 * @author jiaxv
 */
public class Message {
    public static final int PREVIEW = 0;
    public static final int UPLINK = 1;

    public static byte[] uplinkPreview(MainForm mainForm, int mode){
        UplinkProtocol protocol = new UplinkProtocol();
        try {
            protocol.setMode(mainForm.modeInput.getText());
            protocol.setArn(mainForm.arnInput.getText());
            protocol.setLabel(mainForm.labelInput.getText());
            protocol.setDubi(mainForm.dubiInput.getText());
            if(mode == PREVIEW) {
                protocol.setText(mainForm.text.getText().getBytes());
            }else{
                String text = mainForm.text.getText();
                StringBuilder sb = new StringBuilder(text);
                int tag = 0;

                //逐个位置替换成6bit
                for(int i = 0 ; i < text.length(); i++){
                    if(text.charAt(i) < 32){
                        JOptionPane.showMessageDialog(mainForm.mainPanel, "存在不可修正非法字符！");
                        return null;
                    }else if(text.charAt(i) > 95){
                        tag = 1;
                        sb.setCharAt(i, (char)(text.charAt(i) - 32));
                    }
                    sb.setCharAt(i, Util.to6bit(text.charAt(i)));
                }
                if(tag == 1){
                    JOptionPane.showMessageDialog(mainForm.mainPanel, "存在可以修正非法字符，已自动修正！");
                }
                byte[] cryptedtext = CryptoUtil.enCrypt(mainForm.key.getText(), Util.loadCode(sb.toString().getBytes()));
                protocol.setText(cryptedtext);
            }
        }catch (Exception e){
            JOptionPane.showMessageDialog(mainForm.mainPanel, "请按照格式输入内容");
            e.printStackTrace();
        }

        return protocol.getContentData();
    };
}
