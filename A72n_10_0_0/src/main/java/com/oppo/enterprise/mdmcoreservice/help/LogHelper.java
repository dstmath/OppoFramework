package com.oppo.enterprise.mdmcoreservice.help;

import android.os.SystemProperties;
import android.util.Log;
import java.util.HashMap;

public class LogHelper {
    private static boolean sD = true;
    private static boolean sE = true;
    private static boolean sI = false;
    private static boolean sIsDebug = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static String sSeparator = "-->";
    private static String sSpecial = "mdm";
    private static HashMap<String, Boolean> sTags = new HashMap<>();
    private static boolean sV = false;
    private static boolean sW = true;

    public static boolean getTagDebug(String tag) {
        if (sTags.containsKey(tag)) {
            return sTags.get(tag).booleanValue();
        }
        return true;
    }

    public static void d(String tag, String debugInfo) {
        if (sD && sIsDebug && getTagDebug(tag)) {
            Log.d("enterprise.mdmcoreservice---" + tag, sSpecial + sSeparator + debugInfo);
        }
    }

    public static void e(String tag, String debugInfo) {
        if (sE && sIsDebug && getTagDebug(tag)) {
            Log.e("enterprise.mdmcoreservice---" + tag, sSpecial + sSeparator + debugInfo);
        }
    }
}
