package androidx.test.runner;

import android.util.Log;
import androidx.test.internal.runner.RunnerArgs;
import androidx.test.internal.runner.tracker.UsageTracker;
import androidx.test.internal.runner.tracker.UsageTrackerRegistry;
import androidx.test.internal.util.Checks;

public class UsageTrackerFacilitator implements UsageTracker {
    private final boolean shouldTrackUsage;

    public UsageTrackerFacilitator(RunnerArgs runnerArgs) {
        Checks.checkNotNull(runnerArgs, "runnerArgs cannot be null!");
        boolean z = true;
        if (runnerArgs.orchestratorService != null) {
            this.shouldTrackUsage = (runnerArgs.disableAnalytics || !runnerArgs.listTestsForOrchestrator) ? false : z;
        } else {
            this.shouldTrackUsage = !runnerArgs.disableAnalytics;
        }
    }

    public UsageTrackerFacilitator(boolean shouldTrackUsage2) {
        this.shouldTrackUsage = shouldTrackUsage2;
    }

    public boolean shouldTrackUsage() {
        return this.shouldTrackUsage;
    }

    public void registerUsageTracker(UsageTracker usageTracker) {
        if (usageTracker == null || !shouldTrackUsage()) {
            Log.i("UsageTrackerFacilitator", "Usage tracking disabled");
            UsageTrackerRegistry.registerInstance(new UsageTracker.NoOpUsageTracker());
            return;
        }
        Log.i("UsageTrackerFacilitator", "Usage tracking enabled");
        UsageTrackerRegistry.registerInstance(usageTracker);
    }

    @Override // androidx.test.internal.runner.tracker.UsageTracker
    public void trackUsage(String usage, String version) {
        if (shouldTrackUsage()) {
            UsageTrackerRegistry.getInstance().trackUsage(usage, version);
        }
    }

    @Override // androidx.test.internal.runner.tracker.UsageTracker
    public void sendUsages() {
        if (shouldTrackUsage()) {
            UsageTrackerRegistry.getInstance().sendUsages();
        }
    }
}
