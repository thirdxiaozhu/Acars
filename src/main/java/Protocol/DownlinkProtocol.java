package Protocol;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;


public class DownlinkProtocol extends BasicProtocol{
    public static final int PROTOCOL_TYPE = 1;

    private static final int MSN_LEN = 4;
    private static final int ID_LEN = 6;

    private String msn;
    private String flightId;

    private String text;

    @Override
    public int getLength(){
        return super.getLength() + MSN_LEN + ID_LEN + text.getBytes().length;
    }

    public void setMsn(String msn){
        this.msn = msn;
    }
    public String getMsn(){
        return msn;
    }

    public void setFlightId(String flightId){
        this.flightId = flightId;
    }
    public String getFlightId(){
        return flightId;
    }

    public void setText(String text){
        StringBuilder sb = new StringBuilder(text);
        for(int i = 0 ; i < text.length(); i++){
            sb.setCharAt(i, Util.to6bit(text.charAt(i)));
        }
        this.text = sb.toString();
    }

    public String getText(){
        return text;
    }

    @Override
    public byte[] getContentData(){
        byte[] base = super.getContentData();
        byte[] msn = getMsn().getBytes();
        byte[] flightid = getFlightId().getBytes();
        byte[] text = getText().getBytes();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(getLength());
        baos.write(base,0,base.length);
        baos.write(msn,0,MSN_LEN);
        baos.write(msn,0,ID_LEN);
        baos.write(text,0,text.length);
        baos.write(setTail(baos),0,getTailLength());

        return baos.toByteArray();
    }

}
