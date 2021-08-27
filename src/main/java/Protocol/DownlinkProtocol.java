package Protocol;

import javax.swing.*;
import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;


public class DownlinkProtocol extends BasicProtocol implements Protocol , Serializable {
    public static final int PROTOCOL_TYPE = 1;

    private static final int MSN_LEN = 4;
    private static final int ID_LEN = 6;

    private String msn;
    private String flightId;

    private byte[] text;

    @Override
    public int getLength(){
        return super.getLength() + MSN_LEN + ID_LEN + text.length;
    }

    public void setMsn(String msn){
        this.msn = msn;
    }
    public String getMsn(){
        this.msn = "M01A";
        return msn;
    }

    public void setFlightId(String flightId){
        this.flightId = flightId;
    }
    public String getFlightId(){
        return flightId;
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
        if(Util.regularMatch("[A-Za-z]{1}",tak) || "".equals(tak)){
            super.setTak(tak);
        }else {
            JOptionPane.showMessageDialog(null, "下行技术确认格式错误！");
        }
    }

    @Override
    public void setDubi(String dubi){
        if(Util.regularMatch("[0-9]{1}",dubi)){
            super.setDubi(dubi);
        }else{
            JOptionPane.showMessageDialog(null, "下行Ubi格式错误！");
        }
    }

    @Override
    public byte[] getContentData(){
        byte[] base = super.getContentData();
        byte[] msn = getMsn().getBytes();
        byte[] flightid = getFlightId().getBytes();
        byte[] text = getText();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(getLength());
        baos.write(base,0,base.length);
        //baos.write(msn,0,MSN_LEN);
        //baos.write(flightid,0,ID_LEN);
        baos.write(text,0,text.length);
        ByteArrayOutputStream temp = baos;
        baos.write(setTail(temp),0,getTailLength());

        return baos.toByteArray();
    }

}
