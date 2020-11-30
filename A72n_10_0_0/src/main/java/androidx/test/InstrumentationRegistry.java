package androidx.test;

import android.app.Instrumentation;
import android.os.Bundle;
import java.util.concurrent.atomic.AtomicReference;

@Deprecated
public final class InstrumentationRegistry {
    private static final AtomicReference<Bundle> arguments = new AtomicReference<>(null);
    private static final AtomicReference<Instrumentation> instrumentationRef = new AtomicReference<>(null);

    @Deprecated
    public static void registerInstance(Instrumentation instrumentation, Bundle arguments2) {
        instrumentationRef.set(instrumentation);
        arguments.set(new Bundle(arguments2));
    }
}
