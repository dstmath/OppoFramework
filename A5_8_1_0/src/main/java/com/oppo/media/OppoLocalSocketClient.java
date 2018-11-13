package com.oppo.media;

public class OppoLocalSocketClient {
    public static final String SOCKET_COMMON_ADDRESS = "Multimedia_local_socket";
    public static final String SOCKET_DIRAC_ADDRESS = "AudioEffect";
    private static final String TAG = "OppoLocalSocketClient";
    private long mNativeContext;

    private final native void native_finalize();

    private static final native void native_init();

    private final native void native_setup(String str);

    private final native void release();

    public native void sendMessage(String str);

    public native void setEventInfo(int i, String str);

    static {
        System.loadLibrary("OppoLocalSocketClient_jni");
        native_init();
    }

    public OppoLocalSocketClient(String socketname) {
        native_setup(socketname);
    }

    protected void finalize() throws Throwable {
        try {
            native_finalize();
        } finally {
            super.finalize();
        }
    }
}
