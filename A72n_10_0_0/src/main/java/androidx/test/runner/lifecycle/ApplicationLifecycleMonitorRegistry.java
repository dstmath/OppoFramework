package androidx.test.runner.lifecycle;

import java.util.concurrent.atomic.AtomicReference;

public final class ApplicationLifecycleMonitorRegistry {
    private static final AtomicReference<ApplicationLifecycleMonitor> lifecycleMonitor = new AtomicReference<>(null);

    public static ApplicationLifecycleMonitor getInstance() {
        ApplicationLifecycleMonitor instance = lifecycleMonitor.get();
        if (instance != null) {
            return instance;
        }
        throw new IllegalStateException("No lifecycle monitor registered! Are you running under an Instrumentation which registers lifecycle monitors?");
    }

    public static void registerInstance(ApplicationLifecycleMonitor monitor) {
        lifecycleMonitor.set(monitor);
    }
}
