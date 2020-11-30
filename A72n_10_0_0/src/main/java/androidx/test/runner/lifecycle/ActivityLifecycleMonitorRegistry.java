package androidx.test.runner.lifecycle;

import java.util.concurrent.atomic.AtomicReference;

public final class ActivityLifecycleMonitorRegistry {
    private static final AtomicReference<ActivityLifecycleMonitor> lifecycleMonitor = new AtomicReference<>(null);

    public static void registerInstance(ActivityLifecycleMonitor monitor) {
        lifecycleMonitor.set(monitor);
    }
}
