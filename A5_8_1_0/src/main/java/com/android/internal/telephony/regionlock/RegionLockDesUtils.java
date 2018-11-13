package com.android.internal.telephony.regionlock;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class RegionLockDesUtils {
    private static String strDefaultKey = "oppolock";
    private Cipher mDecryptCipher;
    private Cipher mEncryptCipher;

    public static String byteArrToHexStr(byte[] arrB) throws Exception {
        StringBuffer sb = new StringBuffer(iLen * 2);
        for (int intTmp : arrB) {
            int intTmp2;
            while (intTmp2 < 0) {
                intTmp2 += 256;
            }
            if (intTmp2 < 16) {
                sb.append("0");
            }
            sb.append(Integer.toString(intTmp2, 16));
        }
        return sb.toString();
    }

    public static byte[] hexStrToByteArr(String strIn) throws Exception {
        byte[] arrB = strIn.getBytes();
        int iLen = arrB.length;
        byte[] arrOut = new byte[(iLen / 2)];
        for (int i = 0; i < iLen; i += 2) {
            arrOut[i / 2] = (byte) Integer.parseInt(new String(arrB, i, 2), 16);
        }
        return arrOut;
    }

    public RegionLockDesUtils() throws Exception {
        this(strDefaultKey);
    }

    public RegionLockDesUtils(String strKey) throws Exception {
        this.mEncryptCipher = null;
        this.mDecryptCipher = null;
        Key key = getKey(strKey.getBytes());
        this.mEncryptCipher = Cipher.getInstance("DES");
        this.mEncryptCipher.init(1, key);
        this.mDecryptCipher = Cipher.getInstance("DES");
        this.mDecryptCipher.init(2, key);
    }

    public byte[] encrypt(byte[] arrB) throws Exception {
        return this.mEncryptCipher.doFinal(arrB);
    }

    public String encrypt(String strIn) throws Exception {
        return byteArrToHexStr(encrypt(strIn.getBytes()));
    }

    public byte[] decrypt(byte[] arrB) throws Exception {
        return this.mDecryptCipher.doFinal(arrB);
    }

    public String decrypt(String strIn) throws Exception {
        return new String(decrypt(hexStrToByteArr(strIn)));
    }

    private Key getKey(byte[] arrBTmp) throws Exception {
        byte[] arrB = new byte[8];
        int i = 0;
        while (i < arrBTmp.length && i < arrB.length) {
            arrB[i] = arrBTmp[i];
            i++;
        }
        return new SecretKeySpec(arrB, "DES");
    }
}
