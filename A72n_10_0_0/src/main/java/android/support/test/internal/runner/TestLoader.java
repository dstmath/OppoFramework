package android.support.test.internal.runner;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

class TestLoader {

    static class UnloadableClassRunner extends Runner {
        private final Description description;
        private final Failure failure;

        @Override // org.junit.runner.Describable, org.junit.runner.Runner
        public Description getDescription() {
            return this.description;
        }

        @Override // org.junit.runner.Runner
        public void run(RunNotifier notifier) {
            notifier.fireTestStarted(this.description);
            notifier.fireTestFailure(this.failure);
            notifier.fireTestFinished(this.description);
        }
    }
}
