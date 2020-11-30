package androidx.test.internal.runner.tracker;

import androidx.test.internal.runner.tracker.UsageTracker;
import androidx.test.internal.util.Checks;

public final class UsageTrackerRegistry {
    private static volatile UsageTracker instance = new UsageTracker.NoOpUsageTracker();

    public static void registerInstance(UsageTracker tracker) {
        instance = (UsageTracker) Checks.checkNotNull(tracker);
    }

    public static UsageTracker getInstance() {
        return instance;
    }
}
