package android.support.test.runner.intent;

import android.os.Looper;
import android.support.test.internal.util.Checks;
import java.util.concurrent.atomic.AtomicBoolean;

public final class IntentStubberRegistry {
    private static IntentStubber mInstance;
    private static AtomicBoolean mIsLoaded = new AtomicBoolean();

    public static boolean isLoaded() {
        return mIsLoaded.get();
    }

    public static IntentStubber getInstance() {
        checkMain();
        Checks.checkState(mInstance != null, "No intent monitor registered! Are you running under an Instrumentation which registers intent monitors?");
        return mInstance;
    }

    private static void checkMain() {
        Checks.checkState(Looper.myLooper() == Looper.getMainLooper(), "Must be called on main thread.");
    }
}
