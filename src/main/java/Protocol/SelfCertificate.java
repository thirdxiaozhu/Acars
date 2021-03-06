package Protocol;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.spec.ECGenParameterSpec;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author jiaxv
 */
public class SelfCertificate {
    public static final int DSP = 0;
    public static final int CMU = 1;
    public KeyPairDto keyPairDto;
    private final byte[] ID;
    private final byte[] time;
    private final byte[] certificate;
    private final byte[] pubKey;

    public SelfCertificate(String ID) {
        this.ID = String.format("%10s", ID).replace(" ", ".").getBytes();
        this.keyPairDto = generateKeyPair();
        this.time = getTime();
        this.pubKey = keyPairDto.getPublicKey();
        System.out.println(time.length + " " + pubKey.length);
        this.certificate = new byte[this.ID.length + pubKey.length + time.length];
        generateCertificate();
    }

    /**
     * 保存成秘钥库文件
     *
     * @throws Exception
     */
    public static Certificate genCertificate(KeyPair keyPair, String DN, int mode) throws Exception {
        String signaturealg = "SM3WITHSM2";

        Certificate certificate = selfSign(keyPair, signaturealg, DN);

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        char[] password = "cauc".toCharArray();
        keyStore.load(null, password);
        keyStore.setKeyEntry("myeckey", keyPair.getPrivate(), password,
                new Certificate[]{certificate});


        //获取当前系统的“我的文档”文件夹，并生成/cryptogoose文件夹用以存储.keystore文件(Linux下为/home文件夹)
        JFileChooser tempfilechooser = new JFileChooser();
        FileSystemView fw = tempfilechooser.getFileSystemView();
        //拼接字符串，指向我的文档
        String tempPath = fw.getDefaultDirectory().toString() + "/cryptogoose";

        File defaultPath = new File(tempPath);
        //如果我的文档目录下没有cryptogoose文件夹，那么就创建一个cryptogoose文件夹
        if (!defaultPath.exists() && !defaultPath.isDirectory()) {
            defaultPath.mkdir();
        }

        //写入文件
        FileOutputStream fos;
        String suffix = (mode==DSP) ? "_DSP.keystore" : "_CMU.keystore";
        fos = new FileOutputStream(defaultPath + "/" + Util.getTime(1) + suffix);
        keyStore.store(fos, password);


        return certificate;
    }

    /**
     * 生成自签名数字证书
     *
     * @param keyPair      密钥对
     * @param signaturealg 模式
     * @return 证书
     * @throws Exception
     */
    private static Certificate selfSign(KeyPair keyPair, String signaturealg, String DN) throws Exception {
        BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
        Security.addProvider(bouncyCastleProvider);

        long now = System.currentTimeMillis();
        Date startDate = new Date(now);
        X500Name dnName = new X500Name(DN);

        BigInteger certSerialNumber = new BigInteger(Long.toString(now));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.YEAR, 1);
        Date endDate = calendar.getTime();

        ContentSigner contentSigner = new JcaContentSignerBuilder(signaturealg)
                .build(keyPair.getPrivate());

        JcaX509v3CertificateBuilder certificateBuilder = new
                JcaX509v3CertificateBuilder(dnName, certSerialNumber,
                startDate, endDate, dnName, keyPair.getPublic());

        BasicConstraints basicConstraints = new BasicConstraints(true);

        certificateBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"),
                true, basicConstraints);

        return new JcaX509CertificateConverter().setProvider(bouncyCastleProvider)
                .getCertificate(certificateBuilder.build(contentSigner));
    }

    private byte[] getTime() {
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss");
        //自动获取当前时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        return mFormat.format(calendar.getTime()).getBytes();
    }

    private KeyPairDto generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
            keyPairGenerator.initialize(new ECGenParameterSpec("sm2p256v1"));
            KeyPair ecKeyPair = keyPairGenerator.generateKeyPair();
            BCECPublicKey ecPublicKey = (BCECPublicKey) ecKeyPair.getPublic();
            BCECPrivateKey ecPrivateKey = (BCECPrivateKey) ecKeyPair.getPrivate();

            return new KeyPairDto(ecPublicKey, ecPrivateKey);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void generateCertificate() {
        System.arraycopy(ID, 0, certificate, 0, ID.length);
        System.arraycopy(pubKey, 0, certificate, ID.length, pubKey.length);
        System.arraycopy(time, 0, certificate, ID.length + pubKey.length, time.length);
    }

    public byte[] getCertificate() {
        return certificate;
    }

    public KeyPairDto getKeyPairDto() {
        return keyPairDto;
    }
}
