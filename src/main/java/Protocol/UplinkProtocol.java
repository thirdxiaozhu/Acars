package Protocol;

import javax.swing.*;
import java.io.ByteArrayOutputStream;

public class UplinkProtocol extends BasicProtocol {

    public static final int PROTOCOL_TYPE = 0;
    private byte[] text;

    @Override
    public int getLength() {
        return super.getLength() + text.length;
    }

    public void setText(byte[] content) throws Exception {
        this.text = content;
    }
    public byte[] getText(){
        return text;
    }

    @Override
    public void setDubi(String dubi){
        if(Util.regularMatch("[A-Za-z]{1}",dubi)){
            super.setDubi(dubi);
        }else{
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
}
