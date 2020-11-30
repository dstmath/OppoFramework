package android.support.test.internal.util;

import android.support.test.InstrumentationRegistry;
import android.util.Log;

public final class LogUtil {

    /* access modifiers changed from: package-private */
    public interface Supplier<T> {
        T get();
    }

    private static void logDebug(String tag, Supplier<String> msgSupplier, Object... args) {
        if (isLoggable(tag, 3)) {
            Log.d(tag, String.format(msgSupplier.get(), args));
        }
    }

    public static void logDebugWithProcess(String tag, String message, Object... args) {
        logDebug(tag, new LogUtil$$Lambda$1(message), args);
    }

    static final /* synthetic */ String lambda$logDebugWithProcess$1$LogUtil(String message) {
        String currentProcessName = ProcessUtil.getCurrentProcessName(InstrumentationRegistry.getTargetContext());
        StringBuilder sb = new StringBuilder(4 + String.valueOf(message).length() + String.valueOf(currentProcessName).length());
        sb.append(message);
        sb.append(" in ");
        sb.append(currentProcessName);
        return sb.toString();
    }

    private static boolean isLoggable(String tag, int level) {
        if (tag.length() > 23) {
            tag = tag.substring(0, 22);
        }
        return Log.isLoggable(tag, level);
    }
}
