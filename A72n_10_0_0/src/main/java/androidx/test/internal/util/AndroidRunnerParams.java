package androidx.test.internal.util;

import android.app.Instrumentation;
import android.os.Bundle;

public class AndroidRunnerParams {
    private final Bundle bundle;
    private final boolean ignoreSuiteMethods;
    private final Instrumentation instrumentation;
    private final long perTestTimeout;
    private final boolean skipExecution = false;

    public AndroidRunnerParams(Instrumentation instrumentation2, Bundle bundle2, long perTestTimeout2, boolean ignoreSuiteMethods2) {
        this.instrumentation = instrumentation2;
        this.bundle = bundle2;
        this.perTestTimeout = perTestTimeout2;
        this.ignoreSuiteMethods = ignoreSuiteMethods2;
    }

    public Instrumentation getInstrumentation() {
        return this.instrumentation;
    }

    public Bundle getBundle() {
        return this.bundle;
    }

    public long getPerTestTimeout() {
        return this.perTestTimeout;
    }

    public boolean isIgnoreSuiteMethods() {
        return this.ignoreSuiteMethods;
    }
}
