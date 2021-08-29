package Protocol;

import javax.swing.*;
import java.io.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jiaxv
 */
public class SocketUtil {

    private static Map<Integer, String> msgImp = new HashMap<>();

    static {
        msgImp.put(UplinkProtocol.PROTOCOL_TYPE, "UplinkProtocol");
        msgImp.put(DownlinkProtocol.PROTOCOL_TYPE, "DownlinkProtocol");
    }

    /**
     * 将从流中读取的内容转换成BasicProtocol，并继续解析
     * @param data 从流中读取的内容
     * @param mode 模式
     * @return
     */
    public static BasicProtocol parseContentMsg(byte[] data, int mode){
        //String className = msgImp.get(mode);
        BasicProtocol basicProtocol;
        try{
            //basicProtocol = (BasicProtocol) Class.forName(className).newInstance();
            if(mode == DownlinkProtocol.PROTOCOL_TYPE){
                basicProtocol = new DownlinkProtocol();
            }else{
                basicProtocol = new UplinkProtocol();
            }
            basicProtocol.parseContentData(data);
        } catch (Exception e) {
            basicProtocol = null;
            e.printStackTrace();
        }

        return basicProtocol;
    }

    /**
     * 从输入流中读取内容
     * @param inputStream 输入流
     * @param mode 模式
     * @return 返回解析完成的报文
     */
    public static BasicProtocol readFromStream(InputStream inputStream, int mode){
        BasicProtocol protocol = null;
        BufferedInputStream bis;

        try{
            bis = new BufferedInputStream(inputStream);

            int len = 0;
            byte[] content = new byte[250];
            //由于ACARS报文长度不会超过250，所以一次性读取250个字节
            bis.read(content, len, 250);

            //将多余的空元素去除（由于无法直接比较byte和null,那么就和0比较）
            List<Byte> resultList = new ArrayList<>(250);
            for(byte b: content){
                if((b & 0xff) == 0){
                    break;
                }
                resultList.add(b);
            }
            //Object数组转byte[]数组
            Object[] resultObj = resultList.toArray();
            byte[] result = new byte[resultObj.length];
            for(int i = 0; i < resultObj.length; i++){
                result[i] = (byte)resultObj[i];
            }

            protocol = parseContentMsg(result, mode);
        }catch (SocketException e){
            JOptionPane.showMessageDialog(null, "连接已关闭");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return protocol;
    }

    /**
     * 将报文写入输出流中
     * @param protocol 报文
     * @param outputStream 输出流
     */
    public static void write2Stream(BasicProtocol protocol, OutputStream outputStream){
        BufferedOutputStream bos = new BufferedOutputStream(outputStream);
        byte[] bufferData = protocol.getContentData();

        try{
            bos.write(bufferData);
            bos.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 关闭输入流
     * @param is
     */
    public static void closeInputStream(InputStream is){
        try{
            if(is != null){
                is.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 关闭输出流
     * @param os
     */
    public static void closeOutputStream(OutputStream os){
        try{
            if(os != null){
                os.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}