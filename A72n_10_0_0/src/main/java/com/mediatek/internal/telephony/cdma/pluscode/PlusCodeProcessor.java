package com.mediatek.internal.telephony.cdma.pluscode;

import android.telephony.Rlog;
import dalvik.system.PathClassLoader;

public class PlusCodeProcessor {
    private static final String LOG_TAG = "PlusCodeProcessor";
    private static final String PLUS_CODE_IMPL_CLASS_NAME = "com.mediatek.internal.telephony.cdma.pluscode.CdmaPlusCodeUtils";
    private static final String PLUS_CODE_IMPL_PATH = "/system/vendor/framework/via-plugin.jar";
    private static ClassLoader sClassLoader;
    private static final Object sLock = new Object();
    private static IPlusCodeUtils sPlusCodeUtilsInstance;

    public static IPlusCodeUtils getPlusCodeUtils() {
        if (sPlusCodeUtilsInstance == null) {
            synchronized (sLock) {
                if (sPlusCodeUtilsInstance == null) {
                    sPlusCodeUtilsInstance = makePlusCodeUtis();
                }
            }
        }
        log("getPlusCodeUtils sPlusCodeUtilsInstance=" + sPlusCodeUtilsInstance);
        return sPlusCodeUtilsInstance;
    }

    private static IPlusCodeUtils makePlusCodeUtis() {
        sClassLoader = new PathClassLoader(PLUS_CODE_IMPL_PATH, ClassLoader.getSystemClassLoader());
        try {
            return (IPlusCodeUtils) sClassLoader.loadClass(PLUS_CODE_IMPL_CLASS_NAME).getConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (Exception ex) {
            log("makePlusCodeUtis Exception, return default DefaultPlusCodeUtils" + ex);
            return new DefaultPlusCodeUtils();
        }
    }

    private static void log(String string) {
        Rlog.d(LOG_TAG, string);
    }
}
