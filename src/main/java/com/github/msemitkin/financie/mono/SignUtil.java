package com.github.msemitkin.financie.mono;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.util.Base64;

public class SignUtil {

    private SignUtil() {
    }

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static String generateXSign(String pemFilePath, String xTime, String url) {
        try {
            PrivateKey privateKey = loadPrivateKeyFromPem(pemFilePath);

            String dataToSign = xTime + url;
            byte[] data = dataToSign.getBytes(StandardCharsets.UTF_8);

            Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
            signature.initSign(privateKey);
            signature.update(data);
            byte[] signed = signature.sign();

            return Base64.getEncoder().encodeToString(signed);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate X-Sign", e);
        }
    }

    private static PrivateKey loadPrivateKeyFromPem(String filePath) throws Exception {
        try (PEMParser pemParser = new PEMParser(new FileReader(filePath))) {
            Object object = pemParser.readObject();

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            return switch (object) {
                case PEMKeyPair keyPair -> converter.getKeyPair(keyPair).getPrivate();
                case org.bouncycastle.asn1.pkcs.PrivateKeyInfo privateKeyInfo ->
                    converter.getPrivateKey(privateKeyInfo);
                default -> throw new IllegalArgumentException("Unknown key format: " + object.getClass());
            };
        }
    }


}
