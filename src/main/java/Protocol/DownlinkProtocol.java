package Protocol;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jiaxv
 */
public class DownlinkProtocol extends BasicProtocol implements Protocol, Serializable {
    public static final int PROTOCOL_TYPE = 1;

    private static final int MSN_LEN = 4;
    private static final int ID_LEN = 6;

    private byte[] msn;
    private byte[] flightId;

    private byte[] text;
    private byte[] freetext;

    @Override
    public int getLength() {
        return super.getLength() + MSN_LEN + ID_LEN + text.length;
    }

    public byte[] getMsn() {
        this.msn = "M01A".getBytes();
        return msn;
    }

    public void setMsn(String msn) {
        this.msn = msn.getBytes();
    }

    public byte[] getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId.getBytes();
    }

    @Override
    public byte[] getText() {
        return text;
    }

    public void setText(byte[] content) throws Exception {
        this.text = content;
    }

    public byte[] getFreeText() {
        return freetext;
    }

    @Override
    public void setTak(String tak) {
        if (Util.regularMatch("[A-Za-z]{1}", tak) || "".equals(tak)) {
            super.setTak(tak);
        } else {
            JOptionPane.showMessageDialog(null, "下行技术确认格式错误！");
        }
    }

    @Override
    public void setDubi(String dubi) {
        if (Util.regularMatch("[0-9]{1}", dubi)) {
            super.setDubi(dubi);
        } else {
            JOptionPane.showMessageDialog(null, "下行Ubi格式错误！");
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

    public byte[] parseMsn(byte[] text) {
        byte[] result = new byte[4];
        System.arraycopy(text, 0, result, 0, 4);
        return result;
    }

    public byte[] parseFlightID(byte[] text) {
        byte[] result = new byte[6];
        System.arraycopy(text, 4, result, 0, 6);
        return result;
    }

    public byte[] parseFreeText(byte[] text) {
        byte[] result = new byte[text.length - 10];
        System.arraycopy(text, 10, result, 0, text.length - 10);
        return result;
    }

    public byte[] parseMainBody(byte[] src) {
        int temp = 0;
        for (byte b : src) {
            if ((int) b != 0) {
                temp++;
            }
        }

        byte[] result = new byte[temp - 4];
        System.arraycopy(src, 0, result, 0, result.length);
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
        msn = parseMsn(text);
        flightId = parseFlightID(text);
        freetext = parseFreeText(text);
        return pos + text.length;
    }

    @Override
    public int parseContentData(Certificate certificate, String passwd, byte[] data) throws Exception {
        int pos = super.parseContentData(certificate, passwd, data);

        byte[] message = parseMainBody(parseText(data, pos, 1));
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
        msn = parseMsn(text);
        flightId = parseFlightID(text);
        freetext = parseFreeText(text);
        return pos + text.length;
    }

    @Override
    public int parseContentData(byte[] data, boolean isCertificated, SecretKey secretKey) throws Exception {
        int pos = super.parseContentData(data, isCertificated, secretKey);
        text = parseMainBody(parseText(data, pos, 2));
        flightId = parseFlightID(text);
        byte[] rawText = parseFreeText(text);
        if (isCertificated) {
            freetext = CryptoUtil.deCrypt(secretKey, rawText);
        } else {
            freetext = rawText;
        }

        return pos + text.length;
    }
}