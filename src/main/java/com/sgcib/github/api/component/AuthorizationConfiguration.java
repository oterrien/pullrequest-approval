package com.sgcib.github.api.component;

import lombok.Getter;
import org.apache.commons.codec.Charsets;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

@Component
public class AuthorizationConfiguration {

    @Value("${handler.authorization.login}")
    @Getter
    private String technicalUserLogin;

    @Value("${handler.authorization.password}")
    private String technicalUserPassword;

    @Value("${handler.authorization.password-encrypted}")
    private boolean technicalUserPasswordEncrypted;

    @Getter
    private HttpHeaders httpHeaders;

    @PostConstruct
    private void setUp() {

        if (technicalUserPasswordEncrypted)
        {
            try {
                technicalUserPassword = new Crypter().decrypt(technicalUserPassword);
            } catch (Crypter.Exception e) {
                throw new RuntimeException(e);
            }
        }

        String auth = technicalUserLogin + ":" + technicalUserPassword;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth);
        
        this.httpHeaders = new HttpHeaders();
        this.httpHeaders.set("Authorization", authHeader);
        this.httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    /**
     * Cf. https://github.com/oterrien/encrypter-decrypter.git
     */
    public static class Crypter {

        private SecretKey secretKey;

        public Crypter() throws  Exception {

            try {
                byte[] desKeyData = {(byte) 0x04, (byte) 0x01, (byte) 0x07, (byte) 0x04, (byte) 0x02, (byte) 0x08, (byte) 0x02, (byte) 0x01};
                DESKeySpec desKeySpec = new DESKeySpec(desKeyData);
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
                secretKey = keyFactory.generateSecret(desKeySpec);
            } catch (java.lang.Exception e) {
                throw new  Exception("Error while initializing crytper", e);
            }
        }

        public String decrypt(String data) throws  Exception {
            try {
                Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                byte[] text = new BASE64Decoder().decodeBuffer(data);
                byte[] textEncrypted = cipher.doFinal(text);
                return new String(textEncrypted);
            } catch (java.lang.Exception e) {
                throw new Exception("Error while decrypting data : (" + data + ")", e);
            }
        }

        public static class Exception extends java.lang.Exception {

            public Exception(String message) {
                super(message);
            }

            public Exception(String message, Throwable cause) {
                super(message, cause);
            }
        }
    }

}
