package androidx.test.platform.app;

import android.app.Instrumentation;
import android.os.Bundle;
import java.util.concurrent.atomic.AtomicReference;

public final class InstrumentationRegistry {
    private static final AtomicReference<Bundle> arguments = new AtomicReference<>(null);
    private static final AtomicReference<Instrumentation> instrumentationRef = new AtomicReference<>(null);

    public static Instrumentation getInstrumentation() {
        Instrumentation instance = instrumentationRef.get();
        if (instance != null) {
            return instance;
        }
        throw new IllegalStateException("No instrumentation registered! Must run under a registering instrumentation.");
    }

    public static void registerInstance(Instrumentation instrumentation, Bundle arguments2) {
        instrumentationRef.set(instrumentation);
        arguments.set(new Bundle(arguments2));
    }
}
