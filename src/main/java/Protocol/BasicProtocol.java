package Protocol;

import javax.swing.*;
import java.io.ByteArrayOutputStream;

/**
 * @author jiaxv
 */
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
    private byte[] arn;
    private byte tak;
    private byte[] label;
    private byte dubi;

    private String time;
    private String date_time;

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
            this.arn = result.getBytes();
        }else{
            JOptionPane.showMessageDialog(null, "飞机注册号格式错误！");
        }
    }

    public byte[] getArn(){
        return arn;
    }

    public void setLabel(String label){
        if (Util.regularMatch("[A-Z0-9]{2}",label)){
            this.label = label.getBytes();
        }else{
            JOptionPane.showMessageDialog(null, "标签格式错误！");
        }
    }

    public byte[] getLabel(){
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

    public String getTime(){
        return time;
    }

    public String getDateTime(){
        return date_time;
    }

    public byte[] setTail(ByteArrayOutputStream front){

        byte[] suffix = {Util.indexto8bit(7,1)};

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

    /**
     * 填充报文内容
     * @return
     */
    public byte[] getContentData(){
        byte[] head = {Util.indexto8bit(11,2),Util.indexto8bit(10,2),
                Util.indexto8bit(6,1),Util.indexto8bit(6,1)};
        byte[] soh = {Util.indexto8bit(1,0)};
        byte[] mod = {getMode()};
        byte[] arn = getArn();
        byte[] tak = {getTak()};
        byte[] label = getLabel();
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
        return data[HEAD_LEN + SOH_LEN];
    }

    public static byte[] parseArn(byte[] data){
        byte[] result = new byte[7];
        for(int i = 0; i < 7; i++){
            result[i] = data[HEAD_LEN + SOH_LEN + MODE_LEN + i];
        }
        return result;
    }

    public static byte parseTak(byte[] data){
        return data[HEAD_LEN + SOH_LEN + MODE_LEN + ARN_LEN];
    }

    public static byte[] parseLabel(byte[] data){
        byte[] result = new byte[2];
        for(int i = 0; i < 2; i++){
            result[i] = data[HEAD_LEN + SOH_LEN + MODE_LEN + ARN_LEN + TAK_LEN + i];
        }
        return result;
    }

    public static byte parseDubi(byte[] data){
        return data[HEAD_LEN + SOH_LEN + MODE_LEN + ARN_LEN + TAK_LEN + LABEL_LEN];
    }

    /**
     * 解析报文内容
     * @param data
     * @return
     * @throws Exception
     */
    public int parseContentData(byte[] data) throws Exception {
        mode = parseMode(data);
        arn = parseArn(data);
        tak = parseTak(data);
        label = parseLabel(data);
        dubi = parseDubi(data);
        time = Util.getTime(0);
        date_time = Util.getTime(1);

        return HEAD_LEN + SOH_LEN + MODE_LEN +
                ARN_LEN + TAK_LEN + LABEL_LEN + DUBI_LEN + STX_LEN;
    }
}