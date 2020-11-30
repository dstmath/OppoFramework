package android.support.test.runner.lifecycle;

import java.util.concurrent.atomic.AtomicReference;

public final class ActivityLifecycleMonitorRegistry {
    private static final AtomicReference<ActivityLifecycleMonitor> sLifecycleMonitor = new AtomicReference<>(null);

    public static void registerInstance(ActivityLifecycleMonitor monitor) {
        sLifecycleMonitor.set(monitor);
    }
}
