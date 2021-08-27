package Protocol;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jiaxv
 */
public class CryptoUtil {

    private static Key operateKey(String password) throws Exception{
        byte[] passwd = password.getBytes();
        //MD5可生成128位密钥(必须为128位)
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] keyData = md.digest(passwd);
        SecretKeySpec key = new SecretKeySpec(keyData, "SM4");
        return key;
    }

    public static byte[] enCrypt(String passwd, byte[] plaintext) throws Exception{
        Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS5PADDING",
                BouncyCastleProvider.PROVIDER_NAME);

        //加密
        cipher.init(Cipher.ENCRYPT_MODE , operateKey(passwd));

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
