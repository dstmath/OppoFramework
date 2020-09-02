package com.color.favorite;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import com.color.util.ColorLog;

public class ColorFavoriteHandler extends Handler {
    private static final boolean DBG = false;
    private final HandlerThread mThread;

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.color.util.ColorLog.i(boolean, java.lang.String, java.lang.String):void
     arg types: [int, java.lang.String, java.lang.String]
     candidates:
      com.color.util.ColorLog.i(java.lang.String, java.lang.Class<?>, java.lang.Object[]):void
      com.color.util.ColorLog.i(java.lang.String, java.lang.String, java.lang.Throwable):void
      com.color.util.ColorLog.i(java.lang.String, java.lang.String, java.lang.Object[]):void
      com.color.util.ColorLog.i(boolean, java.lang.String, java.lang.String):void */
    public ColorFavoriteHandler(HandlerThread thread, String tag) {
        super(getLooper(thread));
        this.mThread = thread;
        ColorLog.i(false, IColorFavoriteConstans.TAG_UNIFY, "<init>() : " + this.mThread.getName() + " @ " + tag);
    }

    public boolean quit() {
        return this.mThread.quit();
    }

    private static Looper getLooper(HandlerThread thread) {
        Looper looper = thread.getLooper();
        if (looper != null) {
            return looper;
        }
        throw new IllegalThreadStateException("Looper is null of " + thread.getName() + "[" + thread.getThreadId() + "]");
    }
}
