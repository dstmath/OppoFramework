package com.oppo.internal.telephony.explock;

import android.util.Log;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class RegionLockDesUtils {
    private static String strDefaultKey = "oppolock";
    private Cipher decryptCipher;
    private Cipher encryptCipher;

    public static String byteArrToHexStr(byte[] arrB) throws Exception {
        int iLen = arrB.length;
        StringBuffer sb = new StringBuffer(iLen * 2);
        for (int intTmp : arrB) {
            while (intTmp < 0) {
                intTmp += 256;
            }
            if (intTmp < 16) {
                sb.append("0");
            }
            sb.append(Integer.toString(intTmp == 1 ? 1 : 0, 16));
        }
        return sb.toString();
    }

    public static byte[] hexStrToByteArr(String strIn) throws Exception {
        byte[] arrB = strIn.getBytes();
        int iLen = arrB.length;
        byte[] arrOut = new byte[(iLen / 2)];
        for (int i = 0; i < iLen; i += 2) {
            try {
                arrOut[i / 2] = (byte) Integer.parseInt(new String(arrB, i, 2), 16);
            } catch (NumberFormatException e) {
                Log.d("hexStrToByteArr", e.toString());
            } catch (Exception e2) {
                Log.d("hexStrToByteArr", e2.toString());
            }
        }
        return arrOut;
    }

    public RegionLockDesUtils() throws Exception {
        this(strDefaultKey);
    }

    public RegionLockDesUtils(String strKey) throws Exception {
        this.encryptCipher = null;
        this.decryptCipher = null;
        Key key = getKey(strKey.getBytes());
        this.encryptCipher = Cipher.getInstance("DES");
        this.encryptCipher.init(1, key);
        this.decryptCipher = Cipher.getInstance("DES");
        this.decryptCipher.init(2, key);
    }

    public byte[] encrypt(byte[] arrB) throws Exception {
        return this.encryptCipher.doFinal(arrB);
    }

    public String encrypt(String strIn) throws Exception {
        return byteArrToHexStr(encrypt(strIn.getBytes()));
    }

    public byte[] decrypt(byte[] arrB) throws Exception {
        return this.decryptCipher.doFinal(arrB);
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
