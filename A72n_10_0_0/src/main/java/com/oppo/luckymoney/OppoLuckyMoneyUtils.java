package com.oppo.luckymoney;

import android.graphics.Bitmap;
import android.util.Log;

public class OppoLuckyMoneyUtils {
    private static final boolean DEBUG = false;
    private static final String TAG = "OppoLuckyMoneyUtils";
    private static OppoLuckyMoneyUtils mOppoLuckyMoneyUtilsInstance = null;

    private native void nativeInit();

    public native float nativeDetect(Bitmap bitmap);

    private OppoLuckyMoneyUtils() {
    }

    public static OppoLuckyMoneyUtils getInstance() {
        if (mOppoLuckyMoneyUtilsInstance == null) {
            mOppoLuckyMoneyUtilsInstance = new OppoLuckyMoneyUtils();
            if (mOppoLuckyMoneyUtilsInstance.init() == -1) {
                mOppoLuckyMoneyUtilsInstance = null;
            }
        }
        return mOppoLuckyMoneyUtilsInstance;
    }

    public int init() {
        try {
            System.loadLibrary("oppoluckymoney");
            nativeInit();
            return 0;
        } catch (Throwable e) {
            Log.e("LMManager", "load oppoluckymoney failed:" + e.toString());
            return -1;
        }
    }
}
