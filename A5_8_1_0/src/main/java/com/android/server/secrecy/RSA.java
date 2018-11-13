package com.android.server.secrecy;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;

public class RSA {
    private static final String ALGORITHM = "RSA";

    /* JADX WARNING: Removed duplicated region for block: B:12:0x004d A:{Splitter: B:4:0x0019, ExcHandler: java.security.NoSuchAlgorithmException (r0_1 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0052 A:{Splitter: B:6:0x0030, ExcHandler: java.security.NoSuchAlgorithmException (r0_0 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Missing block: B:12:0x004d, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:13:0x004e, code:
            r0.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:14:0x0052, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:15:0x0053, code:
            r0.printStackTrace();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void initRsaKey() {
        try {
            BigInteger modulus;
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            try {
                RSAPublicKeySpec rsaPublicKeySpec = (RSAPublicKeySpec) KeyFactory.getInstance(ALGORITHM).getKeySpec(publicKey, RSAPublicKeySpec.class);
                modulus = rsaPublicKeySpec.getModulus();
                BigInteger publicExponent = rsaPublicKeySpec.getPublicExponent();
            } catch (GeneralSecurityException e) {
            }
            try {
                RSAPrivateKeySpec rsaPrivateKeySpec = (RSAPrivateKeySpec) KeyFactory.getInstance(ALGORITHM).getKeySpec(privateKey, RSAPrivateKeySpec.class);
                modulus = rsaPrivateKeySpec.getModulus();
                BigInteger privateExponent = rsaPrivateKeySpec.getPrivateExponent();
            } catch (GeneralSecurityException e2) {
            }
        } catch (NoSuchAlgorithmException e3) {
            e3.printStackTrace();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x0017 A:{Splitter: B:1:0x0001, ExcHandler: java.security.NoSuchAlgorithmException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:3:0x0017, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:4:0x0018, code:
            r1.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:5:?, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static byte[] shaDigest(String imei) {
        byte[] digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(imei.getBytes("utf-8"));
            return md.digest();
        } catch (Exception e) {
        }
    }

    static byte[] shrink(byte[] data, int length) {
        int pieces = data.length / length;
        byte[] result = new byte[pieces];
        for (int p = 0; p < pieces; p++) {
            result[p] = data[p * length];
            for (int i = 1; i < length; i++) {
                result[p] = (byte) (result[p] ^ data[(p * length) + i]);
            }
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x0012 A:{Splitter: B:1:0x0006, ExcHandler: java.security.NoSuchAlgorithmException (r0_0 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Missing block: B:3:0x0012, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:4:0x0013, code:
            r0.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:5:?, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static PublicKey initRsaPublicKey(BigInteger modulus, BigInteger exponent) {
        PublicKey publicKey = null;
        try {
            return KeyFactory.getInstance(ALGORITHM).generatePublic(new RSAPublicKeySpec(modulus, exponent));
        } catch (GeneralSecurityException e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x0012 A:{Splitter: B:1:0x0006, ExcHandler: java.security.NoSuchAlgorithmException (r0_0 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Missing block: B:3:0x0012, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:4:0x0013, code:
            r0.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:5:?, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static PrivateKey initRsaPrivateKey(BigInteger modulus, BigInteger privateExponent) {
        PrivateKey privateKey = null;
        try {
            return KeyFactory.getInstance(ALGORITHM).generatePrivate(new RSAPrivateKeySpec(modulus, privateExponent));
        } catch (GeneralSecurityException e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x0011 A:{Splitter: B:1:0x0001, ExcHandler: java.security.InvalidKeyException (r2_0 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x0011 A:{Splitter: B:1:0x0001, ExcHandler: java.security.InvalidKeyException (r2_0 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x0011 A:{Splitter: B:1:0x0001, ExcHandler: java.security.InvalidKeyException (r2_0 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x0011 A:{Splitter: B:1:0x0001, ExcHandler: java.security.InvalidKeyException (r2_0 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Missing block: B:3:0x0011, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:4:0x0012, code:
            r2.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:5:?, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static byte[] decrypt(PublicKey publicKey, byte[] cipherText) {
        byte[] deciphered = null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(2, publicKey);
            return cipher.doFinal(cipherText);
        } catch (GeneralSecurityException e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x0011 A:{Splitter: B:1:0x0001, ExcHandler: java.security.InvalidKeyException (r2_0 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x0011 A:{Splitter: B:1:0x0001, ExcHandler: java.security.InvalidKeyException (r2_0 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x0011 A:{Splitter: B:1:0x0001, ExcHandler: java.security.InvalidKeyException (r2_0 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x0011 A:{Splitter: B:1:0x0001, ExcHandler: java.security.InvalidKeyException (r2_0 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Missing block: B:3:0x0011, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:4:0x0012, code:
            r2.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:5:?, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static byte[] encrypt(PrivateKey privateKey, byte[] text) {
        byte[] ciphered = null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(1, privateKey);
            return cipher.doFinal(text);
        } catch (GeneralSecurityException e) {
        }
    }
}
