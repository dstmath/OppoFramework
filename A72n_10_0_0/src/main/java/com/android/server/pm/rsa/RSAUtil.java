package com.android.server.pm.rsa;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;

public class RSAUtil {
    private static final int MAX_DECRYPT_BLOCK = 256;
    private static final int MAX_ENCRYPT_BLOCK = 117;
    public static final String PRIVATE_KEY = "RSAPrivateKey";
    public static final String PUBLIC_KEY = "RSAPublicKey";
    private static final String RSA_KEY_ALGORITHM = "RSA";
    private static final int RSA_KEY_SIZE = 2048;
    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    public static Map<String, String> generateKeys() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(RSA_KEY_ALGORITHM);
            keyPairGen.initialize(RSA_KEY_SIZE);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            Map<String, String> result = new HashMap<>();
            result.put(PUBLIC_KEY, SauBase64.encodeBase64String(((RSAPublicKey) keyPair.getPublic()).getEncoded()));
            result.put(PRIVATE_KEY, SauBase64.encodeBase64String(((RSAPrivateKey) keyPair.getPrivate()).getEncoded()));
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static String publicEncrypt(String source, String publicKey) {
        byte[] cache;
        ByteArrayOutputStream out = null;
        try {
            byte[] data = source.getBytes("UTF-8");
            Key publicK = KeyFactory.getInstance(RSA_KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(SauBase64.decodeBase64(publicKey)));
            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(1, publicK);
            int inputLen = data.length;
            ByteArrayOutputStream out2 = new ByteArrayOutputStream();
            int offSet = 0;
            int i = 0;
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out2.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_ENCRYPT_BLOCK;
            }
            String encodeBase64String = SauBase64.encodeBase64String(out2.toByteArray());
            try {
                out2.close();
            } catch (Exception e) {
            }
            return encodeBase64String;
        } catch (Exception e2) {
            if (0 == 0) {
                return null;
            }
            try {
                out.close();
                return null;
            } catch (Exception e3) {
                return null;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    out.close();
                } catch (Exception e4) {
                }
            }
            throw th;
        }
    }

    public static String privateDecrypt(String source, String privateKey) {
        byte[] cache;
        ByteArrayOutputStream out = null;
        try {
            byte[] encryptedData = SauBase64.decodeBase64(source);
            Key privateK = KeyFactory.getInstance(RSA_KEY_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(SauBase64.decodeBase64(privateKey)));
            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(2, privateK);
            int inputLen = encryptedData.length;
            ByteArrayOutputStream out2 = new ByteArrayOutputStream();
            int offSet = 0;
            int i = 0;
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                    cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
                }
                out2.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_DECRYPT_BLOCK;
            }
            String str = new String(out2.toByteArray(), "UTF-8");
            try {
                out2.close();
            } catch (Exception e) {
            }
            return str;
        } catch (Exception e2) {
            if (0 == 0) {
                return null;
            }
            try {
                out.close();
                return null;
            } catch (Exception e3) {
                return null;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    out.close();
                } catch (Exception e4) {
                }
            }
            throw th;
        }
    }

    public static String publicDecrypt(String source, String publicKey) {
        byte[] cache;
        ByteArrayOutputStream out = null;
        try {
            byte[] encryptedData = SauBase64.decodeBase64(source);
            Key publicK = KeyFactory.getInstance(RSA_KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(SauBase64.decodeBase64(publicKey)));
            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(2, publicK);
            int inputLen = encryptedData.length;
            ByteArrayOutputStream out2 = new ByteArrayOutputStream();
            int offSet = 0;
            int i = 0;
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                    cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
                }
                out2.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_DECRYPT_BLOCK;
            }
            String str = new String(out2.toByteArray(), "UTF-8");
            try {
                out2.close();
            } catch (Exception e) {
            }
            return str;
        } catch (Exception e2) {
            if (0 == 0) {
                return null;
            }
            try {
                out.close();
                return null;
            } catch (Exception e3) {
                return null;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    out.close();
                } catch (Exception e4) {
                }
            }
            throw th;
        }
    }

    public static String privateEncrypt(String source, String privateKey) {
        byte[] cache;
        ByteArrayOutputStream out = null;
        try {
            byte[] data = source.getBytes("UTF-8");
            Key privateK = KeyFactory.getInstance(RSA_KEY_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(SauBase64.decodeBase64(privateKey)));
            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(1, privateK);
            int inputLen = data.length;
            ByteArrayOutputStream out2 = new ByteArrayOutputStream();
            int offSet = 0;
            int i = 0;
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out2.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_ENCRYPT_BLOCK;
            }
            String encodeBase64String = SauBase64.encodeBase64String(out2.toByteArray());
            try {
                out2.close();
            } catch (Exception e) {
            }
            return encodeBase64String;
        } catch (Exception e2) {
            if (0 == 0) {
                return null;
            }
            try {
                out.close();
                return null;
            } catch (Exception e3) {
                return null;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    out.close();
                } catch (Exception e4) {
                }
            }
            throw th;
        }
    }
}
