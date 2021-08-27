package Protocol;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.net.ProtocolException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicProtocol {

    private static final int HEAD_LEN = 4;
    private static final int SOH_LEN = 1;
    private static final int MODE_LEN = 1;
    private static final int ARN_LEN = 7;
    private static final int TAK_LEN = 1;
    private static final int LABEL_LEN = 2;
    private static final int DUBI_LEN = 1;
    private static final int STX_LEN = 1;
    private static final int SUFFIX_LEN = 1;
    private static final int BCS_LEN = 2;
    private static final int BCSSUF_LEN = 1;

    private byte mode;
    private String arn;
    private String label;
    private byte dubi;
    private byte tak;

    public int getLength(){
        return SOH_LEN + MODE_LEN + ARN_LEN + TAK_LEN + LABEL_LEN + DUBI_LEN +
                STX_LEN + SUFFIX_LEN + BCS_LEN + BCSSUF_LEN;
    }
    public int getTailLength(){
        return SUFFIX_LEN + BCS_LEN +BCSSUF_LEN;
    }

    public void setMode(String mode){
        if (Util.regularMatch("[2]{1}",mode)){
            this.mode = mode.getBytes()[0];
        }else{
            JOptionPane.showMessageDialog(null, "模式字符错误！");
        }
    }
    public byte getMode(){
        return mode;
    }

    public void setArn(String arn){
        String result = String.format("%7s",arn).replace(" ",".");
        if (Util.regularMatch("[A-Z0-9-.]{7}", result)){
            this.arn = result;
        }else{
            JOptionPane.showMessageDialog(null, "飞机注册号格式错误！");
        }
    }
    public String getArn(){
        return arn;
    }

    public void setLabel(String label){
        if (Util.regularMatch("[A-Z0-9]{2}",label)){
            this.label = label;
        }else{
            JOptionPane.showMessageDialog(null, "标签格式错误！");
        }
    }
    public String getLabel(){
        return label;
    }

    public void setTak(String tak){
        if("".equals(tak)){
            this.tak = Util.indexto8bit(5,1);
        }else{
            this.tak = tak.getBytes()[0];
        }
    }
    public byte getTak(){
        return tak;
    }

    public void setDubi(String dbi){
        this.dubi = dbi.getBytes()[0];
    }
    public byte getDubi(){
        return dubi;
    }


    public byte[] setTail(ByteArrayOutputStream front){

        byte[] suffix = {Util.indexto8bit(7,1)};

        //front.write(suffix, 0, SUFFIX_LEN);

        byte[] bcs = Util.getCRC16(front.toByteArray());
        byte[] bcssuf = {Util.indexto8bit(15,7)};
        byte[] tail = {Util.indexto8bit(0,1)};

        ByteArrayOutputStream baos = new ByteArrayOutputStream(SUFFIX_LEN+BCS_LEN+BCSSUF_LEN);
        baos.write(suffix, 0, SUFFIX_LEN);
        baos.write(bcs, 0, BCS_LEN);
        baos.write(bcssuf, 0, BCSSUF_LEN);
        baos.write(tail,0,1);
        return baos.toByteArray();
    }


    public byte[] getContentData(){
        byte[] head = {Util.indexto8bit(11,2),Util.indexto8bit(10,2),
                Util.indexto8bit(6,1),Util.indexto8bit(6,1)};
        byte[] soh = {Util.indexto8bit(1,0)};
        byte[] mod = {getMode()};
        byte[] arn = getArn().getBytes();
        byte[] tak = {getTak()};
        byte[] label = getLabel().getBytes();
        byte[] dubi = {getDubi()};
        byte[] stx = {Util.indexto8bit(2,0)};


        ByteArrayOutputStream baos = new ByteArrayOutputStream(getLength());
        baos.write(head,0, HEAD_LEN);
        baos.write(soh,0,SOH_LEN);
        baos.write(mod,0,MODE_LEN);
        baos.write(arn,0,ARN_LEN);
        baos.write(tak,0,TAK_LEN);
        baos.write(label,0,LABEL_LEN);
        baos.write(dubi,0,DUBI_LEN);
        baos.write(stx,0,STX_LEN);
        return baos.toByteArray();
    }

    public static byte parseMode(byte[] data){
        byte m = data[HEAD_LEN + SOH_LEN];
        return m;
    }

    public static String parseArn(byte[] data){
        byte[] result = new byte[2];
        result[0] = data[HEAD_LEN + SOH_LEN + MODE_LEN];
        result[1] = data[HEAD_LEN + SOH_LEN + MODE_LEN + 1];
        System.out.println(Integer.toBinaryString(result[0]&0xff) + " " + Config.TABLE_FOR_8BIT[result[0] & 0x0f][(result[0] >> 4) & 0x07]);
        System.out.println(Integer.toBinaryString(result[1]&0xff) + " " + Config.TABLE_FOR_8BIT[result[1] & 0x0f][(result[1] >> 4) & 0x07]);
        return result.toString();
    }

    public int parseContentData(byte[] data) throws ProtocolException{
        //for(byte b: data){
        //    System.out.println(Integer.toBinaryString(b&0xff) + " " + Config.TABLE_FOR_8BIT[b & 0x0f][(b >> 4) & 0x07]);
        //}
        mode = parseMode(data);
        arn = parseArn(data);
        return 10086;
    }

}
