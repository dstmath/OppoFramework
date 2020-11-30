package android.os;

public class OppoDebug {
    public static final boolean DEBUG_SYSTRACE_TAG = SystemProperties.getBoolean("debug.oppo.systrace_enhance", !IS_RELEASE_VERSION);
    public static boolean IS_RELEASE_VERSION = SystemProperties.getBoolean("ro.build.release_type", false);
    public static final int LOOPER_DELAY = SystemProperties.getInt("debug.oppo.looper_delay", 1000);
    public static final int LOOPER_DELAY_DEFAULT = 1000;
    private static final String TAG = "OppoDebug";
}
