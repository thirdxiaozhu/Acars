package Protocol;

import cauc.MainForm;

import javax.swing.*;

/**
 * @author jiaxv
 */
public class Message {
    public static final int PREVIEW = 0;
    public static final int ENCRYPT = 1;

    public static UplinkProtocol uplinkMessage(MainForm mainForm, int mode){
        UplinkProtocol protocol = new UplinkProtocol();
        try {
            protocol.setMode(mainForm.modeInput.getText());
            protocol.setArn(mainForm.arnInput.getText());
            protocol.setLabel(mainForm.labelInput.getText());
            protocol.setDubi(mainForm.dubiInput.getText());
            protocol.setTak(mainForm.takInput.getText());

            if(mode == PREVIEW) {
                protocol.setText(mainForm.text.getText().getBytes());
            }else{
                String text = mainForm.text.getText();
                StringBuilder sb = new StringBuilder(text);
                if(text.length() > 220){
                    JOptionPane.showMessageDialog(mainForm.mainPanel, "正文长度超过220个字符！");
                    return null;
                }

                //逐个位置替换成6bit
                int tag = 0;
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

        return protocol;
    };

    public static DownlinkProtocol downlinkMessage(MainForm mainForm, int mode){
        DownlinkProtocol protocol = new DownlinkProtocol();
        try {
            protocol.setMode(mainForm.modeInput.getText());
            protocol.setArn(mainForm.arnInput.getText());
            protocol.setLabel(mainForm.labelInput.getText());
            protocol.setDubi(mainForm.dubiInput.getText());
            protocol.setFlightId(mainForm.idInput.getText());
            protocol.setTak(mainForm.takInput.getText());

            String text = "M01A" + mainForm.idInput.getText() + mainForm.text.getText();
            if(mode == PREVIEW) {
                protocol.setText(text.getBytes());
            }else{
                StringBuilder sb = new StringBuilder(text);

                if(text.length() > 220){
                    JOptionPane.showMessageDialog(mainForm.mainPanel, "正文长度超过220个字符！");
                    return null;
                }

                //逐个位置替换成6bit
                int tag = 0;
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

        return protocol;
    };
}