package com.mediatek.custom;

import dalvik.system.PathClassLoader;

public class CustomProperties {
    public static final String HOST_NAME = "HostName";
    public static final String MANUFACTURER = "Manufacturer";
    public static final String MODEL = "Model";
    public static final String MODULE_BLUETOOTH = "bluetooth";
    public static final String MODULE_BROWSER = "browser";
    public static final String MODULE_CMMB = "cmmb";
    public static final String MODULE_DM = "dm";
    public static final String MODULE_FMTRANSMITTER = "fmtransmitter";
    public static final String MODULE_HTTP_STREAMING = "http_streaming";
    public static final String MODULE_MMS = "mms";
    public static final String MODULE_RTSP_STREAMING = "rtsp_streaming";
    public static final String MODULE_SYSTEM = "system";
    public static final String MODULE_WLAN = "wlan";
    public static final int PROP_MODULE_MAX = 32;
    public static final int PROP_NAME_MAX = 64;
    public static final String RDS_VALUE = "RDSValue";
    public static final String SSID = "SSID";
    public static final String UAPROF_URL = "UAProfileURL";
    public static final String USER_AGENT = "UserAgent";
    static ClassLoader mLoader = new PathClassLoader("/system/framework/CustomPropInterface.jar", ClassLoader.getSystemClassLoader());

    private static native String native_get_string(String str, String str2, String str3);

    static {
        System.loadLibrary("custom_jni");
    }

    public static String getString(String name) {
        return getString(null, name, null);
    }

    public static String getString(String module, String name) {
        return getString(module, name, null);
    }

    public static String getString(String module, String name, String defaultValue) {
        if (module != null && module.length() > 32) {
            throw new IllegalArgumentException("module.length >32");
        } else if (name != null && name.length() <= 64) {
            return native_get_string(module, name, defaultValue);
        } else {
            throw new IllegalArgumentException("name.length > 64");
        }
    }

    private static Class loadInterface() {
        Class clazz;
        try {
            clazz = mLoader.loadClass("com.mediatek.custom.CustomPropInterface");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            clazz = null;
        }
        System.out.println("[CustomProp]loadInterface->clazz:" + clazz);
        return clazz;
    }
}
