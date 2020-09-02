package com.oppo.statistics.util;

import android.util.Base64;

public class Base64Util {
    public static String base64Encode(String str) {
        try {
            return new String(Base64.encode(str.getBytes(), 0), "UTF-8");
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
            return "";
        }
    }

    public static String base64Decode(String str) {
        try {
            return new String(Base64.decode(str, 0), "UTF-8");
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
            return "";
        }
    }
}
