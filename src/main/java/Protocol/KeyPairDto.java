package Protocol;


import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

import java.util.Base64;

public class KeyPairDto {
    private BCECPublicKey publicKeyStr;
    private BCECPrivateKey privateKeyStr;


    public KeyPairDto(BCECPublicKey publicKeyStr, BCECPrivateKey privateKeyStr) {
        this.publicKeyStr = publicKeyStr;
        this.privateKeyStr = privateKeyStr;
    }

    @Override
    public String toString() {
        return "KeyPairStrDto{" +
                "publicKeyStr='" + publicKeyStr + '\'' +
                ",\n privateKeyStr='" + privateKeyStr + '\'' +
                '}';
    }

    public byte[] getPublicKey() {
        return publicKeyStr.getEncoded();
    }

    public String getPrivateKeyStr() {
        return Base64.getEncoder().encodeToString(privateKeyStr.getEncoded());
    }

    public BCECPrivateKey getPrivateKey() {
        return privateKeyStr;
    }
}