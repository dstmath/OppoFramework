package com.mediatek.common.jpe;

public class a {
    public static boolean b = false;

    public native int aa();

    static {
        System.loadLibrary("nativecheck-jni");
    }

    public void a() throws JpeException {
        if (!b) {
            b = true;
        }
        if (aa() != 0) {
            throw new JpeException("Class Not Found");
        }
    }
}
