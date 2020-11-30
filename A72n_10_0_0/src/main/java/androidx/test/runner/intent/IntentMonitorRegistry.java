package androidx.test.runner.intent;

import java.util.concurrent.atomic.AtomicReference;

public final class IntentMonitorRegistry {
    private static final AtomicReference<IntentMonitor> monitorRef = new AtomicReference<>(null);

    public static void registerInstance(IntentMonitor monitor) {
        monitorRef.set(monitor);
    }
}
