package com.color.favorite;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import com.color.util.ColorLog;

public class ColorFavoriteHandler extends Handler {
    private static final boolean DBG = false;
    private final HandlerThread mThread;

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
