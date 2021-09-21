package Protocol;

import cauc.MainForm;

import javax.swing.*;

/**
 * @author jiaxv
 */
public class Message {
    public static final int PREVIEW = 0;
    public static final int ENCRYPT = 1;
    public static final int CIPHER_LENGTH = 1;
    public static final int SIGN_LENGTH = 1;

    /**
     * 生成上行报文
     * @param mainForm
     * @param mode
     * @return
     */
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
                //if(text.length() > 220){
                //    JOptionPane.showMessageDialog(mainForm.mainPanel, "正文长度超过220个字符！");
                //    return null;
                //}

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

                //明文
                byte[] plainText = sb.toString().getBytes();
                long starttime = System.nanoTime();
                byte[] cryptedText = CryptoUtil.enCrypt(mainForm.passwd, Util.loadCode(plainText));
                long endtime = System.nanoTime();
                System.out.println("编码 + 加密时间：" + String.valueOf(endtime-starttime));


                //对密文的签名
                starttime = System.nanoTime();
                byte[] signValue = CryptoUtil.signMessage(mainForm.DSPDialog.keyPair.getPrivate(), cryptedText, cryptedText.length);
                endtime = System.nanoTime();
                System.out.println("签名时间：" + String.valueOf(endtime-starttime));


                //密文长度（1byte）+ 签名值长度（1byte）+ 密文 + 明文 组成正文
                byte[] result = new byte[cryptedText.length + signValue.length + CIPHER_LENGTH + SIGN_LENGTH];
                System.out.println(cryptedText.length);
                System.arraycopy(new byte[]{(byte) cryptedText.length, (byte) signValue.length}, 0, result, 0, CIPHER_LENGTH + SIGN_LENGTH);
                System.arraycopy(cryptedText, 0, result, CIPHER_LENGTH + SIGN_LENGTH, cryptedText.length);
                System.arraycopy(signValue, 0, result, CIPHER_LENGTH + SIGN_LENGTH + cryptedText.length, signValue.length);
                System.out.println((int)result[0] + " " + (int)result[1]);

                if(result.length > 220){
                    JOptionPane.showMessageDialog(mainForm.mainPanel, "正文长度超过220个字符！");
                    return null;
                }

                protocol.setText(result);
            }
        }catch (Exception e){
            JOptionPane.showMessageDialog(mainForm.mainPanel, "请按照格式输入内容");
            e.printStackTrace();
        }

        return protocol;
    };

    /**
     * 生成下行报文
     * @param mainForm
     * @param mode
     * @return
     */
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

                //明文
                byte[] plainText = sb.toString().getBytes();
                //密文
                byte[] cryptedText = CryptoUtil.enCrypt(mainForm.passwd, Util.loadCode(plainText));
                //对密文的签名
                byte[] signValue = CryptoUtil.signMessage(mainForm.DSPDialog.keyPair.getPrivate(), cryptedText, cryptedText.length);
                //密文长度（1byte）+ 签名值长度（1byte）+ 密文 + 明文 组成正文
                byte[] result = new byte[cryptedText.length + signValue.length + CIPHER_LENGTH + SIGN_LENGTH];
                System.arraycopy(new byte[]{(byte) cryptedText.length, (byte) signValue.length}, 0, result, 0, CIPHER_LENGTH + SIGN_LENGTH);
                System.arraycopy(cryptedText, 0, result, CIPHER_LENGTH + SIGN_LENGTH, cryptedText.length);
                System.arraycopy(signValue, 0, result, CIPHER_LENGTH + SIGN_LENGTH + cryptedText.length, signValue.length);

                protocol.setText(result);
            }
        }catch (Exception e){
            JOptionPane.showMessageDialog(mainForm.mainPanel, "请按照格式输入内容");
            e.printStackTrace();
        }

        return protocol;
    };
}