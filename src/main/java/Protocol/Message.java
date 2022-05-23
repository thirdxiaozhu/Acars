package Protocol;

import cauc.CMU_MainForm;
import cauc.DSP_MainForm;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.util.Arrays;

/**
 * @author jiaxv
 */
public class Message {
    public static final int NONE = 0;
    public static final int PREVIEW = 1;
    public static final int ENCRYPT = 2;
    public static final int ENCRYPT_MOD_2 = 3;
    public static final int CERTIFICATE = 4;
    public static final int HELLO = 5;
    public static final int SYMMETRICAL_1 = 6;
    public static final int SYMMETRICAL_2 = 7;
    public static final int CYPHER_LENGTH = 1;
    public static final int SIGN_LENGTH = 1;

    /**
     * 生成上行报文
     * @param DSPMainForm
     * @param mode
     * @return
     */
    public static UplinkProtocol uplinkMessage(DSP_MainForm DSPMainForm, int mode){
        return uplinkMessage(DSPMainForm, mode, null);
    }


    public static UplinkProtocol uplinkMessage(DSP_MainForm DSPMainForm, int mode, SecretKey symmetricKey){
        UplinkProtocol protocol = new UplinkProtocol();
        try {
            protocol.setMode(DSPMainForm.modeInput.getText());
            protocol.setArn(DSPMainForm.arnInput.getText());
            protocol.setLabel(DSPMainForm.labelInput.getText());
            protocol.setDubi(DSPMainForm.dubiInput.getText());
            protocol.setTak(DSPMainForm.takInput.getText());

            switch (mode){
                case NONE -> protocol.setText(DSPMainForm.text.getText().getBytes());
                case PREVIEW -> protocol.setText(DSPMainForm.text.getText().getBytes());
                case ENCRYPT -> {
                    String text = DSPMainForm.text.getText();
                    StringBuilder sb = new StringBuilder(text);

                    //逐个位置替换成6bit
                    int tag = 0;
                    for(int i = 0 ; i < text.length(); i++){
                        if(text.charAt(i) < 32){
                            JOptionPane.showMessageDialog(DSPMainForm.mainPanel, "存在不可修正非法字符！");
                            return null;
                        }else if(text.charAt(i) > 95){
                            tag = 1;
                            sb.setCharAt(i, (char)(text.charAt(i) - 32));
                        }
                        sb.setCharAt(i, Util.to6bit(text.charAt(i)));
                    }
                    if(tag == 1){
                        JOptionPane.showMessageDialog(DSPMainForm.mainPanel, "存在可以修正非法字符，已自动修正！");
                    }

                    //明文
                    byte[] plainText = sb.toString().getBytes();
                    long starttime = System.nanoTime();
                    byte[] cryptedText = CryptoUtil.enCrypt(DSPMainForm.passwd, Util.loadCode(plainText));
                    long endtime = System.nanoTime();
                    System.out.println("编码 + 加密时间：" + String.valueOf(endtime-starttime));


                    //对密文的签名
                    starttime = System.nanoTime();
                    byte[] signValue = CryptoUtil.signMessage(DSPMainForm.DSPDialog.keyPair.getPrivate(), cryptedText, cryptedText.length);
                    System.out.println(new String(signValue));
                    System.out.println(signValue.length);
                    endtime = System.nanoTime();
                    System.out.println("签名时间：" + String.valueOf(endtime-starttime));


                    //密文长度（1byte）+ 签名值长度（1byte）+ 密文 + 明文 组成正文
                    byte[] result = new byte[cryptedText.length + signValue.length + CYPHER_LENGTH + SIGN_LENGTH];
                    System.out.println(cryptedText.length);
                    System.arraycopy(new byte[]{(byte) cryptedText.length, (byte) signValue.length}, 0, result, 0, CYPHER_LENGTH + SIGN_LENGTH);
                    System.arraycopy(cryptedText, 0, result, CYPHER_LENGTH + SIGN_LENGTH, cryptedText.length);
                    System.arraycopy(signValue, 0, result, CYPHER_LENGTH + SIGN_LENGTH + cryptedText.length, signValue.length);
                    System.out.println((int)result[0] + " " + (int)result[1]);
                    System.out.println(result.length);

                    if(result.length > 220){
                        JOptionPane.showMessageDialog(DSPMainForm.mainPanel, "正文长度超过220个字符！");
                        return null;
                    }

                    protocol.setText(result);
                }
                case ENCRYPT_MOD_2 -> {
                    byte[] cipherBytes = CryptoUtil.enCrypt(symmetricKey, DSPMainForm.text.getText().getBytes());
                    protocol.setText(LoadCode.getEncoder().encode(cipherBytes));
                }
                case CERTIFICATE -> {
                    byte[] preText = ("RESPOND" + DSPMainForm.idInput.getText()).getBytes();
                    byte[] certificate = DSPMainForm.certificate.getCertificate();
                    byte[] rawBytes = new byte[preText.length+certificate.length];
                    System.arraycopy(preText, 0, rawBytes, 0, preText.length);
                    System.arraycopy(certificate, 0, rawBytes, preText.length, certificate.length);
                    protocol.setText(LoadCode.getEncoder().encode(rawBytes));
                }
            }
        }catch (Exception e){
            JOptionPane.showMessageDialog(DSPMainForm.mainPanel, "请按照格式输入内容");
            e.printStackTrace();
        }

        return protocol;
    }

    /**
     * 生成下行报文
     * @param CMUMainForm
     * @param mode
     * @return
     */
    public static DownlinkProtocol downlinkMessage(CMU_MainForm CMUMainForm, int mode) {
        return downlinkMessage(CMUMainForm, mode, null, null);
    }

    public static DownlinkProtocol downlinkMessage(CMU_MainForm CMUMainForm, SecretKey secretKey) {
        return downlinkMessage(CMUMainForm, ENCRYPT_MOD_2, null, secretKey);
    }

    public static DownlinkProtocol downlinkMessage(CMU_MainForm CMUMainForm, byte[] cryptedSymmetriy){
        return downlinkMessage(CMUMainForm, SYMMETRICAL_1, cryptedSymmetriy, null);
    }

    public static DownlinkProtocol downlinkMessage(CMU_MainForm CMUMainForm, int mode, byte[] cryptedSymmetriy, SecretKey secretKey){
        DownlinkProtocol protocol = new DownlinkProtocol();
        try {
            protocol.setMode(CMUMainForm.modeInput.getText());
            protocol.setArn(CMUMainForm.arnInput.getText());
            protocol.setLabel(CMUMainForm.labelInput.getText());
            protocol.setDubi(CMUMainForm.dubiInput.getText());
            protocol.setFlightId(CMUMainForm.idInput.getText());
            protocol.setTak(CMUMainForm.takInput.getText());

            String preText = "MO1A" + CMUMainForm.idInput.getText();
            String text = preText + CMUMainForm.text.getText();
            byte[] preBytes = preText.getBytes();

            switch (mode){
                case NONE -> protocol.setText(text.getBytes());
                case PREVIEW -> protocol.setText(text.getBytes());
                case ENCRYPT -> {
                    StringBuilder sb = new StringBuilder(text);

                    //正文逐个位置替换成6bit
                    int tag = 0;
                    for(int i = 0 ; i < text.length(); i++){
                        if(text.charAt(i) < 32){
                            JOptionPane.showMessageDialog(CMUMainForm.mainPanel, "存在不可修正非法字符！");
                            return null;
                        }else if(text.charAt(i) > 95){
                            tag = 1;
                            sb.setCharAt(i, (char)(text.charAt(i) - 32));
                        }
                        sb.setCharAt(i, Util.to6bit(text.charAt(i)));
                    }
                    if(tag == 1){
                        JOptionPane.showMessageDialog(CMUMainForm.mainPanel, "存在可以修正非法字符，已自动修正！");
                    }

                    //明文
                    byte[] plainText = sb.toString().getBytes();
                    //密文
                    byte[] cryptedText = CryptoUtil.enCrypt(CMUMainForm.passwd, Util.loadCode(plainText));
                    //对密文的签名
                    byte[] signValue = CryptoUtil.signMessage(CMUMainForm.privateKey, cryptedText, cryptedText.length);
                    //密文长度（1byte）+ 签名值长度（1byte）+ 密文 + 明文 组成正文
                    byte[] result = new byte[cryptedText.length + signValue.length + CYPHER_LENGTH + SIGN_LENGTH];
                    System.arraycopy(new byte[]{(byte) cryptedText.length, (byte) signValue.length}, 0, result, 0, CYPHER_LENGTH + SIGN_LENGTH);
                    System.arraycopy(cryptedText, 0, result, CYPHER_LENGTH + SIGN_LENGTH, cryptedText.length);
                    System.arraycopy(signValue, 0, result, CYPHER_LENGTH + SIGN_LENGTH + cryptedText.length, signValue.length);

                    if(result.length > 220){
                        JOptionPane.showMessageDialog(CMUMainForm.mainPanel, "正文长度超过220个字符！");
                        return null;
                    }

                    protocol.setText(result);
                }
                case ENCRYPT_MOD_2 -> {
                    byte[] cipherBytes = CryptoUtil.enCrypt(secretKey, CMUMainForm.text.getText().getBytes());
                    byte[] rawBytes = new byte[preBytes.length+cipherBytes.length];
                    System.arraycopy(preBytes,0,rawBytes,0,preBytes.length);
                    System.arraycopy(cipherBytes,0,rawBytes,preBytes.length, cipherBytes.length);
                    protocol.setText(LoadCode.getEncoder().encode(rawBytes));
                }
                case HELLO -> {
                    byte[] hello = "HELLO".getBytes();
                    byte[] rawBytes = new byte[preBytes.length+hello.length];
                    System.arraycopy(preBytes,0,rawBytes,0,preBytes.length);
                    System.arraycopy(hello,0,rawBytes,preBytes.length, hello.length);
                    protocol.setText(LoadCode.getEncoder().encode(rawBytes));
                }
                case SYMMETRICAL_1 -> {
                    byte[] rawBytes = new byte[preBytes.length+cryptedSymmetriy.length];
                    System.arraycopy(preBytes,0,rawBytes,0,preBytes.length);
                    System.arraycopy(cryptedSymmetriy,0,rawBytes,preBytes.length, cryptedSymmetriy.length);
                    protocol.setText(LoadCode.getEncoder().encode(rawBytes));
                }
            }
            if(mode == NONE){
                protocol.setText(text.getBytes());
            }else if(mode == PREVIEW) {
                protocol.setText(text.getBytes());
            }else if(mode == ENCRYPT){
            }
        }catch (Exception e){
            JOptionPane.showMessageDialog(CMUMainForm.mainPanel, "请按照格式输入内容");
            e.printStackTrace();
        }

        return protocol;
    }

}