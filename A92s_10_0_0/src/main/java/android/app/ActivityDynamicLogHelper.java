package android.app;

import android.os.SystemProperties;

class ActivityDynamicLogHelper {
    ActivityDynamicLogHelper() {
    }

    static void enableDynamicalLogIfNeed() {
        boolean on = "eng".equals(SystemProperties.get("ro.build.type"));
        String open = SystemProperties.get("sys.activity.thread.log");
        if (open != null) {
            on |= open.equals("true");
        }
        setDynamicalLogEnable(on);
    }

    static void setDynamicalLogEnable(boolean on) {
        OppoMirrorActivityThread.setBooleanValue(OppoMirrorActivityThread.localLOGV, on);
        OppoMirrorActivityThread.setBooleanValue(OppoMirrorActivityThread.DEBUG_BROADCAST, on);
        OppoMirrorActivityThread.setBooleanValue(OppoMirrorActivityThread.DEBUG_SERVICE, on);
        OppoMirrorActivityThread.setBooleanValue(OppoMirrorActivityThread.DEBUG_MESSAGES, on);
        OppoMirrorActivityThread.setBooleanValue(OppoMirrorActivityThread.DEBUG_MEMORY_TRIM, on);
        OppoMirrorActivityThread.setBooleanValue(OppoMirrorActivityThread.DEBUG_BROADCAST_LIGHT, on);
        OppoMirrorActivityThread.setBooleanValue(OppoMirrorActivityThread.DEBUG_CONFIGURATION, on);
        OppoMirrorActivityThread.setBooleanValue(OppoMirrorActivityThread.DEBUG_PROVIDER, on);
        OppoMirrorTransactionExecutor.setBooleanValue(OppoMirrorTransactionExecutor.DEBUG_RESOLVER, on);
    }
}
