package android.os;

public final class OppoJunkLog {
    private static final boolean DEBUG = true;
    public static final int FLAG_DMESG = 4;
    public static final int FLAG_DUMPSTATE = 16;
    public static final int FLAG_LOGCAT = 2;
    public static final int FLAG_SYSTRACE = 8;
    public static final int ID_MULTIMEDIA = 2;
    public static final int ID_PERFORMANCE = 1;
    public static final String TAG = "OppoJunkLog";

    private static native int native_StopCollectCommonLog_Func(int i, String str, String str2, int i2);

    private static native int native_collectCommonLog_Func(int i, String str, String str2, int i2);

    private static native int native_collectModuleLog_Func(int i, String str, String str2, String str3);

    private static native String native_oppoManager_testFunc(int i, int i2);

    public static void testFunc(int id, int size) {
        native_oppoManager_testFunc(id, size);
    }

    public static void collectCommonLog(int moduleId, String issueType, String packageName, int flag) {
        native_collectCommonLog_Func(moduleId, issueType, packageName, flag);
    }

    public static void stopCollectCommonLog(int moduleId, String issueType, String packageName, int flag) {
        native_StopCollectCommonLog_Func(moduleId, issueType, packageName, flag);
    }

    public static void collectModuleLog(int moduleId, String issueType, String packageName, String logPath) {
        native_collectModuleLog_Func(moduleId, issueType, packageName, logPath);
    }
}
