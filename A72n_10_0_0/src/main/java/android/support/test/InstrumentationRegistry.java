package android.support.test;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import java.util.concurrent.atomic.AtomicReference;

public final class InstrumentationRegistry {
    private static final AtomicReference<Bundle> sArguments = new AtomicReference<>(null);
    private static final AtomicReference<Instrumentation> sInstrumentationRef = new AtomicReference<>(null);

    public static Instrumentation getInstrumentation() {
        Instrumentation instance = sInstrumentationRef.get();
        if (instance != null) {
            return instance;
        }
        throw new IllegalStateException("No instrumentation registered! Must run under a registering instrumentation.");
    }

    public static Context getTargetContext() {
        return getInstrumentation().getTargetContext();
    }

    public static void registerInstance(Instrumentation instrumentation, Bundle arguments) {
        sInstrumentationRef.set(instrumentation);
        sArguments.set(new Bundle(arguments));
    }
}
