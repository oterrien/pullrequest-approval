package com.sgcib.github.api.component;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public final class Crypter {

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

    public String encrypt(String data) throws  Exception {
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] text = data.getBytes();
            byte[] textEncrypted = cipher.doFinal(text);
            return new BASE64Encoder().encode(textEncrypted);
        } catch (java.lang.Exception e) {
            throw new  Exception("Error while encrypting data : (" + data + ")", e);
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
