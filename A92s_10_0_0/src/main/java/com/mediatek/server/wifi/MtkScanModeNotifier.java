package com.mediatek.server.wifi;

public class MtkScanModeNotifier {
    private static final String TAG = "MtkScanModeNotifier";

    private static native int registerNatives();

    private static native boolean setScanOnlyModeNative(boolean z);

    static {
        System.loadLibrary("wifi-service");
        registerNatives();
    }

    public static void setScanMode(boolean enable) {
        setScanOnlyModeNative(enable);
    }
}
