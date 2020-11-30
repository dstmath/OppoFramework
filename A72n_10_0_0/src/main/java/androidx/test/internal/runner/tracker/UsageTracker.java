package androidx.test.internal.runner.tracker;

public interface UsageTracker {
    void sendUsages();

    void trackUsage(String str, String str2);

    public static class NoOpUsageTracker implements UsageTracker {
        @Override // androidx.test.internal.runner.tracker.UsageTracker
        public void trackUsage(String unused, String unusedVersion) {
        }

        @Override // androidx.test.internal.runner.tracker.UsageTracker
        public void sendUsages() {
        }
    }
}
