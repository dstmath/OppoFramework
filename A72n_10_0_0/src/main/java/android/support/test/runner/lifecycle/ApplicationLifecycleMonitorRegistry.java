package android.support.test.runner.lifecycle;

import java.util.concurrent.atomic.AtomicReference;

public final class ApplicationLifecycleMonitorRegistry {
    private static final AtomicReference<ApplicationLifecycleMonitor> sLifecycleMonitor = new AtomicReference<>(null);

    public static void registerInstance(ApplicationLifecycleMonitor monitor) {
        sLifecycleMonitor.set(monitor);
    }
}
