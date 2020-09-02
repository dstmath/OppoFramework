package com.oppo.internal.telephony.explock.util;

import android.os.Process;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.oppo.internal.telephony.explock.OemLockUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ExpLockHelper {
    private static int TYPE_DEVICE_LOCK = 1;
    private static int TYPE_REGIONNET_LOCK = 0;
    private static int TYPE_SIMLOCK = 2;

    public static boolean matchUnLock(String imei, String password, int type) {
        if (!checkPerrmission()) {
            return false;
        }
        String result = null;
        if (type == TYPE_REGIONNET_LOCK) {
            result = encryptRegionNetLockImei(imei);
        } else if (type == TYPE_DEVICE_LOCK) {
            result = encryptDeviceLockImei(imei);
        } else if (type == TYPE_SIMLOCK) {
            result = encryptSimLockImei(imei);
        }
        if (imei == null || password == null || !password.equalsIgnoreCase(result)) {
            return false;
        }
        return true;
    }

    private static String encryptSimLockImei(String imei) {
        return newEncryptImeiBySalt(imei, new byte[]{49, 42, 111, 80, 110, 102, 113, 107});
    }

    private static String encryptRegionNetLockImei(String imei) {
        return newEncryptImeiBySalt(imei, new byte[]{94, -101, 103, 26, 52, 47, -120, 102});
    }

    private static String encryptDeviceLockImei(String imei) {
        byte[] salt = {26, 52, 47, 94, -101, 103, -120, 102};
        if (OemLockUtils.isEncryptVersion_1()) {
            return encryptImeiBySalt(imei, salt);
        }
        return newEncryptImeiBySalt(imei, salt);
    }

    private static String encryptImeiBySalt(String imei, byte[] salt) {
        byte[] imeiByteArray = imei.getBytes();
        byte[] key = new byte[16];
        for (int i = 0; i < key.length; i++) {
            if (i == 1) {
                key[i] = salt[0];
            } else if (i == 3) {
                key[i] = salt[1];
            } else if (i == 5) {
                key[i] = salt[2];
            } else if (i == 7) {
                key[i] = salt[3];
            } else if (i == 9) {
                key[i] = salt[4];
            } else if (i == 11) {
                key[i] = salt[5];
            } else if (i == 13) {
                key[i] = salt[6];
            } else if (i != 15) {
                key[i] = imeiByteArray[i];
            } else {
                key[i] = salt[7];
            }
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(key);
            return bytes2Hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (Exception e2) {
            return null;
        }
    }

    private static String newEncryptImeiBySalt(String imei, byte[] salt) {
        byte[] imeiByteArray = imei.getBytes();
        byte[] key = new byte[(imeiByteArray.length + salt.length)];
        for (int i = 0; i < key.length; i++) {
            if (i < imeiByteArray.length) {
                key[i] = imeiByteArray[i];
            } else if (i >= imeiByteArray.length) {
                key[i] = salt[i - imeiByteArray.length];
            }
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(key);
            return encryptSaltAgain(bytes2Hex(md.digest()));
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (Exception e2) {
            return null;
        }
    }

    private static String encryptSaltAgain(String salt) {
        if (TextUtils.isEmpty(salt)) {
            return null;
        }
        byte[] key = salt.getBytes();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(key);
            return bytes2Hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (Exception e2) {
            return null;
        }
    }

    public static String bytes2Hex(byte[] inputbyte) {
        String firstChar;
        String hexResult = "";
        for (int i = 0; i < inputbyte.length; i++) {
            String temp = Integer.toHexString(inputbyte[i] & 255);
            if (temp.length() == 1) {
                firstChar = "0";
            } else {
                firstChar = temp.substring(0, 1);
            }
            if (i % 2 == 0) {
                hexResult = hexResult + firstChar;
            }
        }
        return hexResult;
    }

    private static boolean checkPerrmission() {
        int callingUid = Process.myUid();
        if (callingUid == 1000 || callingUid == 1001) {
            return true;
        }
        Rlog.e("davis", "permission denied");
        return false;
    }
}
