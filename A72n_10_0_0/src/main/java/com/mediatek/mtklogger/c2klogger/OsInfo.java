package com.mediatek.mtklogger.c2klogger;

import android.os.Build;

public class OsInfo {
    public static String getVersion() {
        return Build.VERSION.RELEASE;
    }
}
