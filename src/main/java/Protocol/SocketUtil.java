package Protocol;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
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
