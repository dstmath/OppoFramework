package com.qti.server.power;

public final class SubSystemShutdown {
    private static final String TAG = "SubSystemShutdown";

    public static native int shutdown();

    static {
        System.loadLibrary(TAG);
    }
}
