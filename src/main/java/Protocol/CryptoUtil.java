package Protocol;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jiaxv
 */
public class CryptoUtil {
    /**
     * 对秘钥进行散列，返回SM4所需要的秘钥格式
     * @param password
     * @return
     * @throws Exception
     */
    private static Key operateKey(String password) throws Exception{
        byte[] passwd = password.getBytes();
        //MD5可生成128位密钥(必须为128位)
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] keyData = md.digest(passwd);
        SecretKeySpec key = new SecretKeySpec(keyData, "SM4");
        return key;
    }

    /**
     * 加密
     * @param passwd
     * @param plaintext
     * @return
     * @throws Exception
     */
    public static byte[] enCrypt(String passwd, byte[] plaintext) throws Exception{
        Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS5PADDING",
                BouncyCastleProvider.PROVIDER_NAME);

        //加密
        cipher.init(Cipher.ENCRYPT_MODE , operateKey(passwd));

        //加密后对8bit的结果进行信息编码，对于一个("aabbbbbb")格式的byte，先判断7、8位的关系，如果不为0，那么就拆成("000000aa")/("00bbbbbb")，分别存储
        //同时，为了保证解密的时候区分被拆过的和没被拆过的byte，引入标志位。
        // 可以发现，所有信息编码后的byte，其7、8位必然为0，那么可以将被一分二的byte的第八位置为1( & 0x80 )，没有被拆分的byte第七位置为1( & 0x40 )
        List<Byte> resultList = new ArrayList<>();
        for(byte b: cipher.doFinal(plaintext)){
            if((b & 0xc0) != 0){
                resultList.add((byte)((b >> 6) & 0x03 | 0x80));
                resultList.add((byte)(b & 0x3f | 0x80));
            }else{
                resultList.add((byte)(b & 0xff | 0x40));
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
     * 解密
     * @param passwd
     * @param ciphertext
     * @return
     * @throws Exception
     */
    public static byte[] deCrypt(String passwd, byte[] ciphertext) throws Exception{
        List<Byte> resultList = new ArrayList<>();
        for(int i = 0; i < ciphertext.length; ){
            if((ciphertext[i] & 0x80) != 0 && (ciphertext[i+1] & 0x80) != 0){
                resultList.add((byte)(((ciphertext[i] & 0x03) << 6) + (ciphertext[i+1] & 0x3f)));
                i += 2;
            }else if((ciphertext[i] & 0x40) != 0){
                resultList.add((byte)(ciphertext[i] & 0x3f));
                i++;
            }
        }

        Object[] resultObj = resultList.toArray();
        byte[] result = new byte[resultObj.length];
        for(int i = 0; i < resultObj.length; i++){
            result[i] = (byte)resultObj[i];
        }

        Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS5PADDING",
                BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.DECRYPT_MODE , operateKey(passwd));

        return cipher.doFinal(result);
    }
}