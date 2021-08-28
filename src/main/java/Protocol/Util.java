package Protocol;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jiaxv
 * @date 21/08/28
 */
public class Util {

    public static byte indexto8bit(int row, int col){
        //这里row<<4一定要优先运算
        return (byte)((col<<4) + row);
    }

    /**
     * 8bit转换成6bit
     * @param c 8bit字符
     * @return 6bit字符
     */
    public static char to6bit(char c){
        int ascii = c;
        if(ascii > 95 || ascii < 32){
            return (char)indexto8bit(0,0);
        }else{
            return (char)(((ascii >> 4) - 2) * 16 + ascii % 16);
        }
    }

    /**
     * 正则表达匹配
     * @param mod 匹配模式
     * @param source 源字符串
     * @return 是否匹配
     */
    public static boolean regularMatch(String mod,String source){
        Pattern p = Pattern.compile(mod);
        Matcher m = p.matcher(source);
        return m.matches();
    }

    /**
     * 获取BSC码， 算法为CRC16-1021
     * @param data_arr 需要进行BSC校验的字符数组
     * @return 两个字符的字符数组
     */
    public static byte[] getCRC16(byte[] data_arr){
        int crc16 = 0x0;
        //从第二位开始，因为需要避开<SOH>
        for(int i = 1; i < data_arr.length; i++)
        {
            crc16 = (char)(( crc16 >> 8) | (crc16 << 8));
            crc16 ^= data_arr[i] & 0xFF;
            crc16 ^= (char)(( crc16 & 0xFF) >> 4);
            crc16 ^= (char)(( crc16 << 8) << 4);
            crc16 ^= (char)((( crc16 & 0xFF) << 4) << 1);
        }
        byte[] res = int2ByteArrays(crc16);
        return res;
    }

    /**
     * 专门用来解析BSC
     * @param i 16bit int
     * @return 长度为2的字符数组
     */
    public static byte[] int2ByteArrays(int i) {
        byte[] result = new byte[2];
        result[0] = (byte) (0xff & i);
        result[1] = (byte) (0xff & (i >> 8));
        return result;
    }

    /**
     * 将6bit(0x3f)编码成8bit流(0xff)(压缩成标准的8bit流)
     * @param origin
     * @return
     */
    public static byte[] loadCode(byte[] origin){
        int length = origin.length;
        byte[] res;
        //当位数不是4的倍数的时候，需要多出一位来容纳剩余的bit
        if(length % 4 == 0) {
            res = new byte[(int)(length * 0.75)];
        }else{
            res = new byte[(int)(length * 0.75 + 1)];
        }
        for(int i = 0, r = 0; i < origin.length; i++){
            switch (i % 4){
                case 0:
                    if(i != origin.length - 1){
                        res[r] = (byte)((origin[i] << 2) + (origin[i+1] >> 4));
                    }else{
                        res[r] = (byte)(origin[i] << 2);
                    }
                    r++;
                    break;
                case 1:
                    if(i != origin.length - 1){
                        res[r] = (byte)((origin[i] << 4) + (origin[i+1] >> 2));
                    }else{
                        res[r] = (byte)(origin[i] << 4);
                    }
                    r++;
                    break;
                case 2:
                    if(i != origin.length - 1){
                        res[r] = (byte)((origin[i] << 6) + (origin[i+1]));
                    }else{
                        res[r] = (byte)(origin[i] << 6);
                    }
                    r++;
                    break;
                default:
                    break;
            }
        }

        return res;
    }

    /**
     * 8bit编码(0xff)回到6bit(0x3f)(解压缩)
     * @param origin
     * @return
     */
    public static byte[] deLoadCode(byte[] origin){
        List<Byte> resultList = new ArrayList<>();
        for(int i = 0; i < origin.length; i++){
            switch (i % 3){
                case 0:
                    resultList.add((byte)((origin[i] >> 2) & 0x3f));
                    break;
                case 1:
                    resultList.add((byte)(((origin[i-1] << 4) & 0x30) + ((origin[i] >> 4) & 0x0f)));
                    break;
                case 2:
                    resultList.add((byte)(((origin[i-1] << 2) & 0x3c) + ((origin[i] >> 6) & 0x03)));
                    //如果不是最后一位
                    if(((origin[i]) & 0x3f) != 0){
                        resultList.add((byte)((origin[i]) & 0x3f));
                    }
                    break;
                default:
                    break;
            }
        }

        //Object数组转byte[]数组
        Object[] resultObj = resultList.toArray();
        byte[] result = new byte[resultObj.length];
        for(int i = 0; i < resultObj.length; i++){
            result[i] = (byte)resultObj[i];
        }

        return result;
    }

    /**
     * 预览明文（由于是正文是直接填充未经载荷编码，故按照8bit解析）
     * @param protocol
     * @return
     */
    public static String getUntreatedPlainText(Protocol protocol){
        byte[] text = protocol.getContentData();
        StringBuffer sb = new StringBuffer(text.length);
        for(int i = 4; i < text.length; i++){
            sb.append(Config.TABLE_FOR_8BIT[text[i] & 0x0f][(text[i] >> 4) & 0x07]);
        }
        return sb.toString();
    }

    /**
     * 获取明文,并将正文以6bit信息编码后的可读形式返回(用于预览密文)
     * @param
     * @return
     */
    public static String getCypherText(Protocol protocol){
        byte[] text = protocol.getContentData();
        byte[] temp = deLoadCode(protocol.getText());

        StringBuffer sb = new StringBuffer(temp.length);

        for(int i = 4; i < text.length; i++){
            if(i < 18 || i > text.length-5) {
                sb.append(Config.TABLE_FOR_8BIT[text[i] & 0x0f][(text[i] >> 4) & 0x07]);
            }else {
                sb.append(Config.TABLE_FOR_6BIT[temp[i-17] & 0x0f][(temp[i-17] >> 4) & 0x07]);
            }
        }

        return sb.toString();
    }

    /**
     * 将被解析的ACARS格式报文打印
     */
    public static String getPlainText(BasicProtocol protocol){
        byte[] text = protocol.getContentData();
        StringBuffer sb = new StringBuffer(text.length);

        for(int i = 4; i < text.length; i++){
            if(i < 18 || i > text.length-5) {
                sb.append(Config.TABLE_FOR_8BIT[text[i] & 0x0f][(text[i] >> 4) & 0x07]);
            }else {
                sb.append(Config.TABLE_FOR_6BIT[text[i] & 0x0f][(text[i] >> 4) & 0x07]);
            }
        }

        return sb.toString();
    }

    /**
     * 解析属性，并以8bit或6bit输出
     * @param
     * @return 解析后按照字符串返回以填充textfield
     */
    public static String getAttributes(byte[] bytes, int mode){
        StringBuffer sb = new StringBuffer();

        for(int i = 0; i < bytes.length; i++){
            if(mode == 0) {
                //<NAK> 00010101 (21)
                if(bytes[i] == 21){
                    continue;
                }
                sb.append(Config.TABLE_FOR_8BIT[bytes[i] & 0x0f][(bytes[i] >> 4) & 0x07]);
            }else {
                sb.append(Config.TABLE_FOR_6BIT[bytes[i] & 0x0f][(bytes[i] >> 4) & 0x07]);
            }
        }

        return sb.toString();
    }

    /**
     * 获取DSP转发时间，mode为0时，获取的是带毫秒的时间，mode为0时，是ARINC620规定的“D:HH:MM”格式
     * @param mode 0, 1
     * @return
     */
    public static String getTime(int mode){
        Date date = new Date();
        SimpleDateFormat sdf;
        if(mode == 0){
            sdf = new SimpleDateFormat("HH:mm:ss:SSS");
        }else{
            sdf = new SimpleDateFormat("d:HH:mm");
        }
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }
}
