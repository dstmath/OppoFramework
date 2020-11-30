package android.support.test.runner.intent;

import java.util.concurrent.atomic.AtomicReference;

public final class IntentMonitorRegistry {
    private static final AtomicReference<IntentMonitor> mMonitorRef = new AtomicReference<>(null);

    public static void registerInstance(IntentMonitor monitor) {
        mMonitorRef.set(monitor);
    }
}
