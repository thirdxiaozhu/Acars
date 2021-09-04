package Protocol;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jiaxv
 */
public class UplinkProtocol extends BasicProtocol implements Protocol{

    public static final int PROTOCOL_TYPE = 0;
    private byte[] text;

    @Override
    public int getLength() {
        return super.getLength() + text.length;
    }

    public void setText(byte[] content) throws Exception {
        this.text = content;
    }
    @Override
    public byte[] getText(){
        return text;
    }


    @Override
    public void setTak(String tak){
        if(Util.regularMatch("[0-9]{1}",tak) || "".equals(tak)){
            super.setTak(tak);
        }else {
            JOptionPane.showMessageDialog(null, "上行技术确认格式错误！");
        }
    }

    @Override
    public void setDubi(String dubi){
        if(Util.regularMatch("[A-Za-z]{1}",dubi)){
            super.setDubi(dubi);
        } else {
            JOptionPane.showMessageDialog(null, "上行Ubi格式错误！");
        }
    }

    @Override
    public byte[] getContentData(){
        byte[] base = super.getContentData();
        byte[] text = getText();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(getLength());
        baos.write(base,0,base.length);
        baos.write(text,0,text.length);
        ByteArrayOutputStream temp = baos;
        baos.write(setTail(temp),0,getTailLength());

        return baos.toByteArray();
    }

    public byte[] parseText(byte[] data, int pos){
        List<Byte> resultList = new ArrayList<>();
        for(int i = 0; i < data.length - pos - 4; i++){
            resultList.add(data[pos+i]);
        }

        Object[] resultObj = resultList.toArray();
        byte[] result = new byte[resultObj.length];
        for(int i = 0; i < resultObj.length; i++){
            result[i] = (byte)resultObj[i];
        }
        return result;
    }

    @Override
    public int parseContentData(Certificate certificate, String passwd, byte[] data) throws Exception {
        int pos = super.parseContentData(certificate, passwd, data);
        byte[] message = parseText(data, pos);
        byte[] cypherText = new byte[(int)message[0]];
        byte[] signValue = new byte[(int)message[1]];
        System.arraycopy(message, 2, cypherText, 0, cypherText.length);
        System.arraycopy(message, 2+cypherText.length, signValue, 0, signValue.length);
        if(CryptoUtil.verifyMessage(certificate, cypherText, signValue)){
            text = Util.deLoadCode(CryptoUtil.deCrypt(passwd, cypherText));
        }else {
            JOptionPane.showMessageDialog(null, "该消息已被篡改！");
            text = new byte[]{};
        }
        return pos + text.length;
    }
}
