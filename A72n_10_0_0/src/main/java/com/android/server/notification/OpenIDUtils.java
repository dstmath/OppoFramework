package com.android.server.notification;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class OpenIDUtils {
    private static final boolean DEBUG_INTERNAL = false;
    private static final String TAG = "OpenID";

    public static native String nativeGenerateAPID(String str, String str2);

    public static native String nativeGenerateAUID(String str);

    public static native String nativeGenerateGUID();

    public static native String nativeGenerateOUID();

    static {
        System.loadLibrary("IDHelper");
    }

    public static String generateOUID() {
        try {
            return nativeGenerateOUID();
        } catch (Exception e) {
            Log.d(TAG, "generateOUID--error:" + e.getMessage());
            return "";
        }
    }

    public static String generateAUID(String pkg) {
        try {
            return nativeGenerateAUID(pkg);
        } catch (Exception e) {
            Log.d(TAG, "generateAUID--error:" + e.getMessage());
            return "";
        }
    }

    public static String generateGUID() {
        try {
            return nativeGenerateGUID();
        } catch (Exception e) {
            Log.d(TAG, "generateGUID--error:" + e.getMessage());
            return "";
        }
    }

    public static String generateAPID(String key, String guid) {
        try {
            return nativeGenerateAPID(key, guid);
        } catch (Exception e) {
            Log.d(TAG, "generateAPID--error:" + e.getMessage());
            return "";
        }
    }

    private static String generateID(String data) {
        String randomUUID = generateUUID();
        String sha1Data = getMessageDigest(data, "SHA-1");
        if (sha1Data.length() > 16) {
            sha1Data = sha1Data.substring(0, 16);
        }
        String dst = randomUUID + sha1Data;
        String md5Data = getMessageDigest(dst, OpenID.MD5);
        int length = md5Data.length();
        if (length > 16) {
            md5Data = md5Data.substring(length - 16);
        }
        return dst + md5Data;
    }

    public static String getMessageDigest(String origin, String type) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(type);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return bytesToHex(digest.digest(origin.getBytes(StandardCharsets.UTF_8)));
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    public static String getPcbNumber() {
        String serial = SystemProperties.get("gsm.serial", "");
        String vendorSerial = SystemProperties.get("vendor.gsm.serial", "");
        return serial + vendorSerial;
    }

    public static String getSerialNumber() {
        return SystemProperties.get("ro.serialno", "unknown");
    }

    public static String getDeviceId(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
            return telephonyManager != null ? telephonyManager.getDeviceId() : "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getWifiMac(Context context) {
        try {
            WifiInfo wifiInfo = ((WifiManager) context.getApplicationContext().getSystemService("wifi")).getConnectionInfo();
            return (wifiInfo == null || !wifiInfo.hasRealMacAddress()) ? DEBUG_INTERNAL : true ? wifiInfo.getMacAddress() : "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getSHA256(String origin) {
        return getMessageDigest(origin, "SHA-256");
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(b & 255);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
