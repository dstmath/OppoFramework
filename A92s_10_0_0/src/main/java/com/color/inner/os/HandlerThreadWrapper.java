package com.color.inner.os;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class HandlerThreadWrapper {
    private static final String TAG = "HandlerThreadWrapper";

    public static Handler getThreadHandler(HandlerThread handlerThread) {
        try {
            return handlerThread.getThreadHandler();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
