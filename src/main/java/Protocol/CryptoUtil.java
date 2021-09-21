package Protocol;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.cert.Certificate;

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

        //MD5可生成128位散列值(必须为128位)
        //SM3可生成256位散列值，需要截取
        //MessageDigest md = MessageDigest.getInstance("MD5");
        MessageDigest md = MessageDigest.getInstance("SM3");
        byte[] keyData = md.digest(passwd);
        System.out.println("keyData: " + keyData.length);
        byte[] temp = new byte[16];
        System.arraycopy(keyData, 0, temp, 0, 16);

        SecretKeySpec key = new SecretKeySpec(temp, "SM4");
        return key;
    }

    /**
     * 加密
     * @param plaintext
     * @return
     * @throws Exception
     */
    public static byte[] enCrypt(String passwd, byte[] plaintext) throws Exception{
        Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS5PADDING",
                BouncyCastleProvider.PROVIDER_NAME);

        //加密
        cipher.init(Cipher.ENCRYPT_MODE , operateKey(passwd));
        return cipher.doFinal(plaintext);
    }

    /**
     * 解密
     * @param ciphertext
     * @return
     * @throws Exception
     */
    public static byte[] deCrypt(String passwd, byte[] ciphertext) throws Exception{
        Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS5PADDING",
                BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.DECRYPT_MODE , operateKey(passwd));

        return cipher.doFinal(ciphertext);
    }

    /**
     * 签名
     * @param privateKey 私钥
     * @param cryptedText 密文
     * @param length 密文长度
     * @return 签名值
     * @throws Exception
     */
    public static byte[] signMessage(PrivateKey privateKey, byte[] cryptedText, int length) throws Exception {
        Signature signature = Signature.getInstance("SM3WITHSM2");
        signature.initSign(privateKey);
        signature.update(cryptedText,0, length);

        return signature.sign();
    }

    /**
     * 验证签名
     * @param certificate 数字证书获取公钥
     * @param cypherText 密文
     * @param signValue 签名之
     * @return 布尔值
     * @throws Exception
     */
    public static boolean verifyMessage(Certificate certificate, byte[] cypherText, byte[] signValue) throws Exception {
        Signature signature = Signature.getInstance("SM3WITHSM2");
        signature.initVerify(certificate);
        signature.update(cypherText, 0, cypherText.length);
        return signature.verify(signValue);
    }
}