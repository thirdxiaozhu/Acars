package Protocol;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
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
        MessageDigest md = MessageDigest.getInstance("SM3");
        byte[] keyData = md.digest(passwd);
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
     * 加密
     * @param plaintext
     * @return
     * @throws Exception
     */
    public static byte[] enCrypt(SecretKey key, byte[] plaintext) throws Exception{
        Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS5PADDING",
                BouncyCastleProvider.PROVIDER_NAME);

        //加密
        cipher.init(Cipher.ENCRYPT_MODE, key);
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
     * 解密
     * @param ciphertext
     * @return
     * @throws Exception
     */
    public static byte[] deCrypt(SecretKey secretKey, byte[] ciphertext) throws Exception{
        Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS5PADDING",
                BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

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

    public static SecretKey getSymmetricalKey(){
        try {
            KeyGenerator kg = KeyGenerator.getInstance("SM4");
            //下面调用方法的参数决定了生成密钥的长度，可以修改为128, 192或256
            kg.init(128);
            SecretKey sk = kg.generateKey();
            return sk;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] encryptSymmetricalKey(byte[] data, PublicKey publicKey){
        ECPublicKeyParameters localECPublicKeyParameters = null;

        if (publicKey instanceof BCECPublicKey)
        {
            BCECPublicKey localECPublicKey = (BCECPublicKey)publicKey;
            ECParameterSpec localECParameterSpec = localECPublicKey.getParameters();
            ECDomainParameters localECDomainParameters = new ECDomainParameters(
                    localECParameterSpec.getCurve(), localECParameterSpec.getG(),
                    localECParameterSpec.getN());
            localECPublicKeyParameters = new ECPublicKeyParameters(localECPublicKey.getQ(),
                    localECDomainParameters);
        }
        SM2Engine localSM2Engine = new SM2Engine();
        localSM2Engine.init(true, new ParametersWithRandom(localECPublicKeyParameters,
                new SecureRandom()));
        byte[] arrayOfByte2;
        try {
            arrayOfByte2 = localSM2Engine.processBlock(data, 0, data.length);
            return arrayOfByte2;
        }
        catch (InvalidCipherTextException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] decryptSymmetricalKey(PrivateKey privateKey, byte[] cipherData){

        BCECPrivateKey bcecPrivateKey = (BCECPrivateKey) privateKey;
        ECParameterSpec ecParameterSpec = bcecPrivateKey.getParameters();

        ECDomainParameters ecDomainParameters = new ECDomainParameters(ecParameterSpec.getCurve(),
                ecParameterSpec.getG(), ecParameterSpec.getN());

        ECPrivateKeyParameters ecPrivateKeyParameters = new ECPrivateKeyParameters(bcecPrivateKey.getD(),
                ecDomainParameters);

        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(false, ecPrivateKeyParameters);

        String result = null;
        try {
            byte[] arrayOfBytes = sm2Engine.processBlock(cipherData, 0, cipherData.length);
            return arrayOfBytes;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("SM2解密时出现异常");
            return null;
        }
    }
}