package Protocol;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jiaxv
 */
public class UplinkProtocol extends BasicProtocol implements Protocol {

    public static final int PROTOCOL_TYPE = 0;
    private byte[] text;

    @Override
    public int getLength() {
        return super.getLength() + text.length;
    }

    @Override
    public byte[] getText() {
        return text;
    }

    public void setText(byte[] content) throws Exception {
        this.text = content;
    }

    @Override
    public void setTak(String tak) {
        if (Util.regularMatch("[0-9]{1}", tak) || "".equals(tak)) {
            super.setTak(tak);
        } else {
            JOptionPane.showMessageDialog(null, "上行技术确认格式错误！");
        }
    }

    @Override
    public void setDubi(String dubi) {
        if (Util.regularMatch("[A-Za-z]{1}", dubi)) {
            super.setDubi(dubi);
        } else {
            JOptionPane.showMessageDialog(null, "上行Ubi格式错误！");
        }
    }

    @Override
    public byte[] getContentData() {
        byte[] base = super.getContentData();
        byte[] text = getText();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(getLength());
        baos.write(base, 0, base.length);
        baos.write(text, 0, text.length);
        ByteArrayOutputStream temp = baos;
        baos.write(setTail(temp), 0, getTailLength());

        return baos.toByteArray();
    }

    public byte[] parseText(byte[] data, int pos, int mod) {
        List<Byte> resultList = new ArrayList<>();
        for (int i = 0; i < data.length - pos - 4; i++) {
            resultList.add(data[pos + i]);
        }

        Object[] resultObj = resultList.toArray();
        byte[] result = new byte[resultObj.length];
        for (int i = 0; i < resultObj.length; i++) {
            result[i] = (byte) resultObj[i];
        }
        return result;
    }

    public byte[] parseFreeText(byte[] text) {
        int temp = 0;
        for (byte b : text) {
            if ((int) b != 0) {
                temp++;
            }
        }
        byte[] result_t = new byte[temp];
        System.arraycopy(text, 0, result_t, 0, result_t.length);

        byte[] result = new byte[temp - 4];
        System.arraycopy(text, 0, result, 0, result.length);
        return LoadCode.getDecoder().decode(result);
    }

    public byte[] parseNoneText(byte[] text){
        int temp = 0;
        for (byte b : text) {
            if ((int) b != 0) {
                temp++;
            }
        }

        byte[] result = new byte[temp - 4];
        System.arraycopy(text, 0, result, 0, result.length);
        return result;
    }

    @Override
    public int parseContentData(byte[] data) throws Exception {
        int pos = super.parseContentData(data);
        text = parseNoneText(parseText(data, pos, 0));

        return pos + text.length;
    }


    @Override
    public int parseContentData(Certificate certificate, String passwd, byte[] data) throws Exception {
        int pos = super.parseContentData(certificate, passwd, data);
        byte[] message = parseFreeText(parseText(data, pos, 1));
        byte[] cypherText = new byte[(int) message[0]];
        byte[] signValue = new byte[(int) message[1]];
        System.arraycopy(message, 2, cypherText, 0, cypherText.length);
        System.arraycopy(message, 2 + cypherText.length, signValue, 0, signValue.length);
        if (CryptoUtil.verifyMessage(certificate, cypherText, signValue)) {
            text = Util.deLoadCode(CryptoUtil.deCrypt(passwd, cypherText));
        } else {
            JOptionPane.showMessageDialog(null, "该消息已被攻击！");
            text = new byte[]{};
        }
        return pos + text.length;
    }

    @Override
    public int parseContentData(Certificate certificate, List<String> signValueList, String passwd, byte[] data) throws Exception {
        int pos = super.parseContentData(certificate, signValueList, passwd, data);
        //byte[] message = parseText(data, pos, 1);
        byte[] message = parseFreeText(parseText(data, pos, 1));
        byte[] cypherText = new byte[(int) message[0] & 0xFF];
        byte[] signValue = new byte[(int) message[1] & 0xFF];
        System.arraycopy(message, 2, cypherText, 0, cypherText.length);
        System.arraycopy(message, 2 + cypherText.length, signValue, 0, signValue.length);

        for (String s : signValueList) {
            if (s.equals(Arrays.toString(signValue))) {
                JOptionPane.showMessageDialog(null, "该消息被重放！");
                return pos + (new byte[]{}.length);
            } else {
                signValueList.add(Arrays.toString(signValue));
            }
        }

        if (CryptoUtil.verifyMessage(certificate, cypherText, signValue)) {
            long starttime = System.nanoTime();
            text = Util.deLoadCode(CryptoUtil.deCrypt(passwd, cypherText));
            long endtime = System.nanoTime();
        } else {
            JOptionPane.showMessageDialog(null, "该消息已被攻击！");
            text = new byte[]{};
        }
        return pos + text.length;
    }

    @Override
    public int parseContentData(byte[] data, boolean isCertificated, SecretKey secretKey) throws Exception {
        int pos = super.parseContentData(data, isCertificated, secretKey);
        if (isCertificated) {
            text = CryptoUtil.deCrypt(secretKey, parseFreeText(parseText(data, pos, 2)));
        } else {
            text = parseFreeText(parseText(data, pos, 2));
        }

        return pos + text.length;
    }

}
