package Protocol;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.MessageDigest;

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
        cipher.init(Cipher.ENCRYPT_MODE , operateKey(passwd));

        return cipher.doFinal(plaintext);
    }

    public static byte[] deCrypt(String passwd, byte[] ciphertext) throws Exception{
        Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS5PADDING",
                BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.DECRYPT_MODE , operateKey(passwd));

        return cipher.doFinal(ciphertext);
    }
}
