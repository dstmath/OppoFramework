package com.oppo.statistics.util;

import android.util.Base64;

public class Base64Util {
    public static String base64Encode(String str) {
        String encode = "";
        try {
            return new String(Base64.encode(str.getBytes(), 0), "UTF-8");
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
            return encode;
        }
    }

    public static String base64Decode(String str) {
        String decode = "";
        try {
            return new String(Base64.decode(str, 0), "UTF-8");
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
            return decode;
        }
    }
}
