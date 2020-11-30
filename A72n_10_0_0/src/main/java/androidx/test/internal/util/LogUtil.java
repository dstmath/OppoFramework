package androidx.test.internal.util;

import android.util.Log;
import androidx.test.internal.util.ProcSummary;

public final class LogUtil {
    private static volatile String myProcName = null;

    /* access modifiers changed from: package-private */
    public interface Supplier<T> {
        T get();
    }

    private static void logDebug(String tag, Supplier<String> msgSupplier, Object... args) {
        if (isLoggable(tag, 3)) {
            Log.d(tag, String.format(msgSupplier.get(), args));
        }
    }

    static final /* synthetic */ String lambda$logDebugWithProcess$1$LogUtil(String message) {
        String procName = procName();
        StringBuilder sb = new StringBuilder(4 + String.valueOf(message).length() + String.valueOf(procName).length());
        sb.append(message);
        sb.append(" in ");
        sb.append(procName);
        return sb.toString();
    }

    public static void logDebugWithProcess(String tag, String message, Object... args) {
        logDebug(tag, new LogUtil$$Lambda$1(message), args);
    }

    private static final String procName() {
        String procDesc;
        String procDesc2 = myProcName;
        if (procDesc2 != null) {
            return procDesc2;
        }
        try {
            procDesc = ProcSummary.summarize("self").cmdline;
        } catch (ProcSummary.SummaryException e) {
            procDesc = "unknown";
        }
        if (procDesc.length() <= 64 || !procDesc.contains("-classpath")) {
            return procDesc;
        }
        return "robolectric";
    }

    private static boolean isLoggable(String tag, int level) {
        if (tag.length() > 23) {
            tag = tag.substring(0, 22);
        }
        return Log.isLoggable(tag, level);
    }
}
