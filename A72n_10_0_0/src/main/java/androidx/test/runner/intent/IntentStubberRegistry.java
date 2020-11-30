package androidx.test.runner.intent;

import android.os.Looper;
import androidx.test.internal.util.Checks;
import java.util.concurrent.atomic.AtomicBoolean;

public final class IntentStubberRegistry {
    private static IntentStubber instance;
    private static AtomicBoolean isLoaded = new AtomicBoolean();

    public static boolean isLoaded() {
        return isLoaded.get();
    }

    public static IntentStubber getInstance() {
        checkMain();
        Checks.checkState(instance != null, "No intent monitor registered! Are you running under an Instrumentation which registers intent monitors?");
        return instance;
    }

    private static void checkMain() {
        Checks.checkState(Looper.myLooper() == Looper.getMainLooper(), "Must be called on main thread.");
    }
}
