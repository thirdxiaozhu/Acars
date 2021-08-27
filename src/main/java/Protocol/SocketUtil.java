package Protocol;

import java.io.*;
import java.util.*;

/**
 * @author jiaxv
 */
public class SocketUtil {

    private static Map<Integer, String> msgImp = new HashMap<>();

    static {
        msgImp.put(UplinkProtocol.PROTOCOL_TYPE, "UplinkProtocol");
        msgImp.put(DownlinkProtocol.PROTOCOL_TYPE, "DownlinkProtocol");
    }

    public static BasicProtocol parseContentMsg(byte[] data, int mode){
        String className = msgImp.get(mode);
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

    public static BasicProtocol readFromStream(InputStream inputStream, int mode){
        BasicProtocol protocol;
        BufferedInputStream bis;

        System.out.println("读取到！");

        try{
            bis = new BufferedInputStream(inputStream);

            int len = 0;
            byte[] content = new byte[250];
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

            System.out.println(new String(result));

            protocol = parseContentMsg(result, mode);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return protocol;
    }

    public static void write2Stream(BasicProtocol protocol, OutputStream outputStream){
        BufferedOutputStream bos = new BufferedOutputStream(outputStream);
        byte[] bufferData = protocol.getContentData();

        try{
            bos.write(bufferData);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

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
