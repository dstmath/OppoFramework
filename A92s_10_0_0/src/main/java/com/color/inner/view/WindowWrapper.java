package com.color.inner.view;

import android.util.Log;
import android.view.Window;

public class WindowWrapper {
    public static final String TAG = "WindowWrapper";

    public static boolean isDestroyed(Window window) {
        try {
            return window.isDestroyed();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static void setCloseOnTouchOutside(Window window, boolean close) {
        window.setCloseOnTouchOutside(close);
    }
}
