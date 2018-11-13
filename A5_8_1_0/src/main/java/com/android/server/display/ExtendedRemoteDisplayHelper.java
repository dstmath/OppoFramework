package com.android.server.display;

import android.content.Context;
import android.media.RemoteDisplay.Listener;
import android.os.Handler;
import android.util.Slog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class ExtendedRemoteDisplayHelper {
    private static final String TAG = "ExtendedRemoteDisplayHelper";
    private static Class sExtRemoteDisplayClass;
    private static Method sExtRemoteDisplayDispose;
    private static Method sExtRemoteDisplayListen;

    ExtendedRemoteDisplayHelper() {
    }

    static {
        try {
            sExtRemoteDisplayClass = Class.forName("com.qualcomm.wfd.ExtendedRemoteDisplay");
        } catch (Throwable th) {
            Slog.i(TAG, "ExtendedRemoteDisplay Not available.");
        }
        if (sExtRemoteDisplayClass != null) {
            Slog.i(TAG, "ExtendedRemoteDisplay Is available. Find Methods");
            try {
                sExtRemoteDisplayListen = sExtRemoteDisplayClass.getDeclaredMethod("listen", new Class[]{String.class, Listener.class, Handler.class, Context.class});
            } catch (Throwable th2) {
                Slog.i(TAG, "ExtendedRemoteDisplay.listen Not available.");
            }
            try {
                sExtRemoteDisplayDispose = sExtRemoteDisplayClass.getDeclaredMethod("dispose", new Class[0]);
            } catch (Throwable th3) {
                Slog.i(TAG, "ExtendedRemoteDisplay.dispose Not available.");
            }
        }
    }

    public static Object listen(String iface, Listener listener, Handler handler, Context context) {
        Object extRemoteDisplay = null;
        Slog.i(TAG, "ExtendedRemoteDisplay.listen");
        if (sExtRemoteDisplayListen == null || sExtRemoteDisplayDispose == null) {
            return extRemoteDisplay;
        }
        try {
            return sExtRemoteDisplayListen.invoke(null, new Object[]{iface, listener, handler, context});
        } catch (InvocationTargetException e) {
            Slog.i(TAG, "ExtendedRemoteDisplay.listen - InvocationTargetException");
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            } else if (cause instanceof Error) {
                throw ((Error) cause);
            } else {
                throw new RuntimeException(e);
            }
        } catch (IllegalAccessException e2) {
            Slog.i(TAG, "ExtendedRemoteDisplay.listen -IllegalAccessException");
            e2.printStackTrace();
            return extRemoteDisplay;
        }
    }

    public static void dispose(Object extRemoteDisplay) {
        Slog.i(TAG, "ExtendedRemoteDisplay.dispose");
        try {
            sExtRemoteDisplayDispose.invoke(extRemoteDisplay, new Object[0]);
        } catch (InvocationTargetException e) {
            Slog.i(TAG, "ExtendedRemoteDisplay.dispose - InvocationTargetException");
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            } else if (cause instanceof Error) {
                throw ((Error) cause);
            } else {
                throw new RuntimeException(e);
            }
        } catch (IllegalAccessException e2) {
            Slog.i(TAG, "ExtendedRemoteDisplay.dispose-IllegalAccessException");
            e2.printStackTrace();
        }
    }

    public static boolean isAvailable() {
        if (sExtRemoteDisplayClass == null || sExtRemoteDisplayDispose == null || sExtRemoteDisplayListen == null) {
            Slog.i(TAG, "ExtendedRemoteDisplay isAvailable() : Not Available.");
            return false;
        }
        Slog.i(TAG, "ExtendedRemoteDisplay isAvailable() : Available.");
        return true;
    }
}
